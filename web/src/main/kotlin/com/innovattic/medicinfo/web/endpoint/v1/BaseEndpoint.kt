package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.web.security.verifyAuthentication
import org.springframework.beans.factory.annotation.Autowired
import java.security.Principal

abstract class BaseEndpoint {
    @Autowired private lateinit var userDao: UserDao

    fun queryAuthenticatedUser() = verifyAuthentication().resolveUser(userDao)
    fun queryAuthenticatedUser(principal: Principal) = verifyAuthentication(principal).resolveUser(userDao)
}
