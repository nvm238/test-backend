package com.innovattic.medicinfo.logic.triage

import com.innovattic.common.nowInZone
import com.innovattic.medicinfo.logic.dto.ServiceAvailableDto
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.format.annotation.DateTimeFormat
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ConstructorBinding
@ConfigurationProperties(prefix = "feature")
data class MedicinfoServiceHoursProperties(
    private var openingTimes: Map<String, Map<String, List<String>>>?,
    private var holidays: Map<String, List<String>>
) {
    private val parsedOpeningTimes: Map<String, MedicinfoServiceHours>? =
        openingTimes?.let { times -> times.mapValues { MedicinfoServiceHours(it.value) } }

    private val parsedHolidays: Map<String, List<LocalDate>> =
        holidays.mapValues { it.value.map(LocalDate::parse) }

    fun getServiceAvailability(clock: Clock, labelCode: String): ServiceAvailableDto {
        if (parsedOpeningTimes == null) return ServiceAvailableDto(true)

        val medicinfoServiceHours: MedicinfoServiceHours = parsedOpeningTimes[labelCode]
            ?: parsedOpeningTimes["default"]
            ?: return ServiceAvailableDto(true)

        val holidaysForLabel = parsedHolidays[labelCode] ?: parsedHolidays["default"] ?: emptyList()

        return medicinfoServiceHours.getServiceAvailability(clock, holidaysForLabel)
    }
}

data class MedicinfoOpeningTimes(
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    val opening: LocalTime,
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    val closing: LocalTime,
)

data class MedicinfoServiceHours(
   private val rawServiceHours: Map<String, List<String>>
) {
    private val monday: List<MedicinfoOpeningTimes>
    private val tuesday: List<MedicinfoOpeningTimes>
    private val wednesday: List<MedicinfoOpeningTimes>
    private val thursday: List<MedicinfoOpeningTimes>
    private val friday: List<MedicinfoOpeningTimes>
    private val saturday: List<MedicinfoOpeningTimes>
    private val sunday: List<MedicinfoOpeningTimes>
    private val holiday: List<MedicinfoOpeningTimes>

    init {
        monday = convertToOpeningTimes(rawServiceHours["monday"] ?: rawServiceHours["weekday"]!!)
        tuesday = convertToOpeningTimes(rawServiceHours["tuesday"] ?: rawServiceHours["weekday"]!!)
        wednesday = convertToOpeningTimes(rawServiceHours["wednesday"] ?: rawServiceHours["weekday"]!!)
        thursday = convertToOpeningTimes(rawServiceHours["thursday"] ?: rawServiceHours["weekday"]!!)
        friday = convertToOpeningTimes(rawServiceHours["friday"] ?: rawServiceHours["weekday"]!!)
        saturday = convertToOpeningTimes(rawServiceHours["saturday"] ?: rawServiceHours["weekend"]!!)
        sunday = convertToOpeningTimes(rawServiceHours["sunday"] ?: rawServiceHours["weekend"]!!)
        holiday = convertToOpeningTimes(rawServiceHours["holiday"] ?: rawServiceHours["weekend"]!!)
    }

    fun getServiceAvailability(clock: Clock, holidays: List<LocalDate>): ServiceAvailableDto {
        val zonedDateTime = clock.nowInZone(ZoneId.of("Europe/Amsterdam"))

        // Find which set of opening times from the config file is valid for this date
        val openingTimes = getOpeningTimesForDate(zonedDateTime, holidays)

        // If the date is within opening times, just return true for the service availability
        return if (isWithinOpeningTimes(zonedDateTime, openingTimes)) {
            ServiceAvailableDto(true)
        } else {
            // Otherwise start looking for the next available opening time
            getNextAvailableServiceWindow(zonedDateTime, holidays) ?: ServiceAvailableDto(false)
        }
    }

    private fun getNextAvailableServiceWindow(
        zonedDateTime: ZonedDateTime,
        holidays: List<LocalDate>
    ): ServiceAvailableDto? {

        var checkDateTime = zonedDateTime
        var openingTimes = getOpeningTimesForDate(checkDateTime, holidays)

        // Search for maximum 14 days. If there is no opening times within 14 days, it's likely there won't be any.
        for (numberOfDays in 1..14) {
            // First check if there are more opening times later today, then for the upcoming 14 days
            nextOpeningTimeOnSameDayAfter(checkDateTime, openingTimes)?.let {
                val nextOpeningLocalDatetime = LocalDateTime.of(checkDateTime.toLocalDate(), it.opening)
                val nextOpeningZonedDateTime = nextOpeningLocalDatetime
                    .atZone(ZoneId.of("Europe/Amsterdam"))
                    .truncatedTo(ChronoUnit.SECONDS)

                return ServiceAvailableDto(false, nextOpeningZonedDateTime.withZoneSameInstant(ZoneOffset.UTC))
            }

            checkDateTime = zonedDateTime.plusDays(numberOfDays.toLong()).withHour(0).withMinute(0).withSecond(0).withNano(0)
            openingTimes = getOpeningTimesForDate(checkDateTime, holidays)
        }
        return null
    }

    private fun convertToOpeningTimes(openingTimesList: List<String>): List<MedicinfoOpeningTimes> {
        return openingTimesList
            .map {
                val times = it.split("-")
                val openingTime = LocalTime.parse(times[0])
                val closingTime = LocalTime.parse(times[1])

                MedicinfoOpeningTimes(openingTime, closingTime)
            }
            .sortedBy {
                it.opening
            }
    }

    private fun isHoliday(zonedDateTime: ZonedDateTime, holidays: List<LocalDate>) = zonedDateTime.toLocalDate() in holidays

    private fun getOpeningTimesForDayOfWeek(dayOfWeek: DayOfWeek): List<MedicinfoOpeningTimes> {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> monday
            DayOfWeek.TUESDAY -> tuesday
            DayOfWeek.WEDNESDAY -> wednesday
            DayOfWeek.THURSDAY -> thursday
            DayOfWeek.FRIDAY -> friday
            DayOfWeek.SATURDAY -> saturday
            DayOfWeek.SUNDAY -> sunday
        }
    }
    private fun isWithinOpeningTimes(zonedDateTime: ZonedDateTime, openingTimes: List<MedicinfoOpeningTimes>): Boolean {
        return openingTimes
            .any { isWithinOpeningTimes(zonedDateTime, it) }
    }

    private fun nextOpeningTimeOnSameDayAfter(
        zonedDateTime: ZonedDateTime,
        openingTimes: List<MedicinfoOpeningTimes>
    ): MedicinfoOpeningTimes? {
        // Since openingTimes is ordered chronologically, the first opening time is returned
        for (openingTime in openingTimes) {
            if (zonedDateTime.toLocalTime().isBefore(openingTime.opening)) {
                return openingTime
            }
        }
        return null
    }

    private fun getOpeningTimesForDate(date: ZonedDateTime, holidays: List<LocalDate>): List<MedicinfoOpeningTimes> {
        if (isHoliday(date, holidays)) {
            return holiday
        } else {
            return getOpeningTimesForDayOfWeek(date.toLocalDate().dayOfWeek)
        }
    }

    private fun isWithinOpeningTimes(zonedDateTime: ZonedDateTime, openingTimes: MedicinfoOpeningTimes): Boolean =
        zonedDateTime.toLocalTime().isAfter(openingTimes.opening) && zonedDateTime.toLocalTime().isBefore(openingTimes.closing)
}
