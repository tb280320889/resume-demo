package com.github.sherlock.resumedemo.security.jwt

import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Created by TangBin on 2017/9/28.
 */

class JWTConfigurer(private val tokenProvider: TokenProvider) : SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity?) {
        val customFilter = JWTFilter(tokenProvider)
        http!!.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter::class.java)
    }

    companion object {

        val AUTHORIZATION_HEADER = "Authorization"
    }
}
