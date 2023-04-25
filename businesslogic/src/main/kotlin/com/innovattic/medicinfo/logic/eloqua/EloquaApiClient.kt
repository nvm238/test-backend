package com.innovattic.medicinfo.logic.eloqua

import com.innovattic.common.client.ClientLoggingFilter
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder

class EloquaApiClient(
    private val eloquaUrl: String,
    private val eloquaSiteId: String,
    private val eloquaFormName: String
) {

    private val client: Client = ClientBuilder.newBuilder()
        .register(ClientLoggingFilter())
        .build()

    fun sendEmailOptIn(email: String) {
        var target = client.target(eloquaUrl)
        target = target.queryParam("elqFormName", eloquaFormName)
        target = target.queryParam("elqSiteID", eloquaSiteId)
        target = target.queryParam("emailAddress", email)

        target.request().get()
    }
}
