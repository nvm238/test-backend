package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ServiceHoursTest : BaseTriageEndpointTest() {

    @BeforeEach
    fun beforeEach() {
        clock.reset()
    }

    @AfterEach
    fun afterEach() {
        clock.reset()
    }
/*
CZdirect.monday=08:00-21:40
CZdirect.tuesday=08:00-12:00,13:00-21:40
CZdirect.wednesday=08:00-21:40
CZdirect.thursday=09:00-20:40
CZdirect.friday=
CZdirect.saturday=11:00-20:40
CZdirect.sunday=10:00-21:40

CZdirect.holiday=02:00-12:00

Test cases
- Default label
    - Monday morning (closed), opening time same morning
    - Monday opened, return service available
    - Monday evening (closed), opening time next morning
    - Tuesday opened, return service available
    - Tuesday midday closed, opening time next hour
    - Tuesday opened afternoon, return service available
    - Tuesday evening (closed), opening time next morning
    - Wednesday evening (closed), opening time next morning
    - Thursday evening (closed), opening time next Saturday

    - Saturday 24-12-2022 evening closed (last day before holiday), opening time on holiday
    - Sunday 25-12-2022 evening closed (on holiday, before holiday), opening time on holiday
    - Sunday 25-12-2022 opened, return service available
    - Monday 26-12-2022 evening closed, opening time on next morning

    - MEDDWF, Sunday 25-12-2022 closed, return opening time next Friday (first non holiday)
 */

    @Test
    fun `given Monday morning (closed), opening time same morning`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 26, 7, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 9, 26, 8, 0, 0)))
    }

    @Test
    fun `given Monday opened, return service available`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 26, 9, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertTrue(availability.serviceAvailable)
    }

    @Test
    fun `given Monday evening (closed), opening time next morning`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 26, 22, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 9, 27, 8, 0, 0)))
    }

    @Test
    fun `given Tuesday opened, return service available`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 27, 9, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertTrue(availability.serviceAvailable)
    }

    @Test
    fun `given Tuesday midday closed, opening time next hour`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 27, 12, 30,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 9, 27, 13, 0, 0)))
    }

    @Test
    fun `given Tuesday opened afternoon, return service available`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 26, 14, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertTrue(availability.serviceAvailable)
    }

    @Test
    fun `given Tuesday evening (closed), opening time next morning`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 27, 22, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 9, 28, 8, 0, 0)))
    }

    @Test
    fun `given Wednesday evening (closed), opening time next morning`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 28, 22, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 9, 29, 9, 0, 0)))
    }

    @Test
    fun `given Thursday evening (closed), opening time next Saturday`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 9, 29, 22, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 10, 1, 11, 0, 0)))
    }

    @Test
    fun `given Saturday 24-12-2022 evening closed (last day before holiday), opening time on holiday`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 24, 22, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 12, 25, 2, 0, 0)))
    }

    @Test
    fun `given Sunday 25-12-2022 evening closed (on holiday, before holiday), opening time on holiday`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 13, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 12, 26, 2, 0, 0)))
    }

    @Test
    fun `given Sunday 25-12-2022 opened, return service available`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 3, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertTrue(availability.serviceAvailable)
    }

    @Test
    fun `given Monday 26-12-2022 evening closed, opening time on next morning`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 26, 13, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "CZdirect")
        assertFalse(availability.serviceAvailable)
        assertEquals(availability.nextOpeningTime, convertTimeFromAmsterdamTimezoneToUTC(LocalDateTime.of(2022, 12, 27, 8, 0, 0)))
    }

    @Test
    fun `MEDDWF, given Sunday 25-12-2022 open because of no holidays for MEDDWF`() {
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 13, 0,0)))
        val availability = medicinfoServiceHoursProperties.getServiceAvailability(clock, "MEDDWF")
        assertTrue(availability.serviceAvailable)
    }

    private fun convertTimeToAmsterdamTimezone(dateTime: LocalDateTime): ZonedDateTime {
        return dateTime.atZone(ZoneId.of("Europe/Amsterdam"))
    }

    private fun convertTimeFromAmsterdamTimezoneToUTC(dateTime: LocalDateTime): ZonedDateTime {
        return dateTime.atZone(ZoneId.of("Europe/Amsterdam"))
            .withZoneSameInstant(ZoneOffset.UTC)
    }
}
