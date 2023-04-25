package com.innovattic.medicinfo.logic

import com.innovattic.medicinfo.database.dto.ShelterLocationDto
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.springframework.stereotype.Component

@Component
class ShelterLocationService(
    private val salesforceService: SalesforceService,
) {

    fun getShelterLocations(): List<ShelterLocationDto> {
        return salesforceService.getShelterLocations()
    }
}
