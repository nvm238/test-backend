package com.innovattic.medicinfo.logic

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dto.GeneralPracticeCenterDto
import com.innovattic.medicinfo.database.dto.GeneralPracticeDto
import com.innovattic.medicinfo.database.dto.GeneralPracticePractitionerDto
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class GeneralPracticeService(
    private val salesforceService: SalesforceService,
    private val labelDao: LabelDao,
) {
    fun getGeneralPractices(labelCode: String?, contracted: Boolean): List<GeneralPracticeDto> {
        if (labelCode != null) {
            labelDao.getByCode(labelCode)
                ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with code $labelCode not found" }
        }
        return salesforceService.getGeneralPractices(contracted, labelCode)
    }

    fun getGeneralPracticePractitioners(generalPracticeCode: String, labelCode: String?): List<GeneralPracticePractitionerDto> {
        verifyLabel(labelCode)
        return salesforceService.getGeneralPracticePractitioners(generalPracticeCode)
    }

    fun getGeneralPracticeCenters(labelCode: String?, contracted: Boolean): List<GeneralPracticeCenterDto> {
        verifyLabel(labelCode)
        return salesforceService.getGeneralPracticeCenters(contracted)
    }

    private fun verifyLabel(labelCode: String?) {
        labelCode ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Label code should be provided" }

        // Verify label exists, but it is not used yet in the salesforce service
        labelDao.getByCode(labelCode)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with code $labelCode not found" }
    }
}
