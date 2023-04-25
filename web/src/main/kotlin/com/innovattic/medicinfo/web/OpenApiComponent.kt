package com.innovattic.medicinfo.web

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
open class OpenApiComponent(@Value("\${medicinfo.api.public.url:}") val url: String) {

    companion object {
        const val SECURITY_APIKEY = "apikey"
        const val SECURITY_JWT = "jwt"
    }

    @Bean
    open fun openApiDefinition(): OpenAPI = OpenAPI()
        // Springdoc doesn't know we're behind https; aws takes care of that, so set server url to force https.
        // (proper fix would be to enable 'server.use-forward-headers=true' but that will probably also require a
        // rewrite of ip whitelisting functionality)
        // Using the OpenApiCustomiser did not work when apidoc caching is enabled as it would use the
        // generated url (which is not https) when serving the cached version.
        // https://github.com/springdoc/springdoc-openapi/issues/1188
        .apply {
            if (url.isNotEmpty()) {
                this.servers(listOf(Server().url(url)))
            }
        }
        .components(
            Components()
                .addSecuritySchemes(
                    SECURITY_APIKEY,
                    SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name(HttpHeaders.AUTHORIZATION)
                        .description(
                            """
                            Use this authentication method to get an access token for the API, using the endpoints
                            under `/authentication`. The value must be an API key corresponding to the user for which
                            you are requesting a token.
                        """.trimIndent()
                        )
                )
                .addSecuritySchemes(
                    SECURITY_JWT,
                    SecurityScheme()
                        .name("JWT Token")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description(
                            """
                            Use this authentication method once you have an access token, previously obtained
                            by the `/authentication` endpoints. A token is user-bound.
                            """.trimIndent()
                        )
                )
        )
        // enable jwt authentication for all endpoints
        // the apikey endpoints are marked individually
        .addSecurityItem(SecurityRequirement().addList(SECURITY_JWT))
        .info(
            Info()
                .title("MedicInfo API")
                .description(
                    """
                    Welcome to the interactive API documentation of the MedicInfo API.
                    
                    All endpoints require authentication via an Authorization header with value `Bearer <accessToken>`,
                    unless specified otherwise. The access token is obtained from the endpoints under `authentication-endpoint`.
                    To get a JWT token as regular json response, use the `authentication-token/token` endpoint. For web pages,
                    you can start a secure (http-only) cookie session by using the `authentication/session` endpoint.
                    
                    For interactive use of this documentation, first use the Authorize button and
                    specify your API key under `apikey`. Then use the
                    authentication endpoint to get a JWT token. Finally, press Authorize again and fill in the JWT token. You
                    can now try out all endpoints.
                    """.trimIndent()
                )
        )
        .externalDocs(
            ExternalDocumentation()
                .description("Gitlab API documentation (Innovattic internal only)")
                .url("https://gitlab.innovattic.com/medicinfo/medicinfo-backend/-/tree/develop/apidoc")
        )
}
