package com.github.sherlock.resumedemo.security

import org.springframework.security.core.AuthenticationException

/**
 * Created by TangBin on 2017/9/28.
 *
 * This exception is thrown in case of a not activated user trying to authenticate.
 */
class UserNotActivatedException : AuthenticationException {

    constructor(message: String) : super(message)

    constructor(message: String, t: Throwable) : super(message, t)

    companion object {
        private val serialVersionUID = -584891231547L
    }
}
