package com.innovattic.medicinfo.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.error.DetailedJsonExceptionHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import java.time.Clock

@ControllerAdvice
class AppExceptionHandler(
    mapper: ObjectMapper,
    clock: Clock,
    @Value("\${medicinfo.errors.hideinternal:false}")
    private var hideInternalErrors: Boolean
) : DetailedJsonExceptionHandler(mapper, clock, hideInternalErrors)
