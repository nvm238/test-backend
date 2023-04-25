package com.innovattic.medicinfo.logic

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dto.HolidayDestinationDto
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class HolidayDestinationService(
    private val salesforceService: SalesforceService,
    private val labelDao: LabelDao,
) {

    fun getHolidayDestinations(labelCode: String?): List<HolidayDestinationDto> {
        labelCode ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Label code should be provided" }

        // Verify label exists, but it is not used yet in the salesforce service
        labelDao.getByCode(labelCode)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with id $labelCode not found" }

        return salesforceService.getHolidayDestinations()
    }
}
