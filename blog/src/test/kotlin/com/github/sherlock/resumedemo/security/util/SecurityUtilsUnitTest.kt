package com.github.sherlock.resumedemo.security.util

import com.github.sherlock.resumedemo.security.AuthoritiesConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * Created by TangBin on 2017/9/28.
 */

internal class SecurityUtilsUnitTest {

    @Test
    fun testGetCurrentUserLogin() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val login = SecurityUtils.getCurrentUserLogin()
        assertThat(login).isEqualTo("admin")
    }

    @Test
    fun testGetCurrentUserJWT() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "token")
        SecurityContextHolder.setContext(securityContext)
        val jwt = SecurityUtils.getCurrentUserJWT()
        assertThat(jwt).isEqualTo("token")
    }

    @Test
    fun testIsAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = SecurityUtils.isAuthenticated()
        assertThat(isAuthenticated).isTrue()
    }

    @Test
    fun testAnonymousIsNotAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS))
        securityContext.authentication = UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities)
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = SecurityUtils.isAuthenticated()
        assertThat(isAuthenticated).isFalse()
    }

    @Test
    fun testIsCurrentUserInRole() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority(AuthoritiesConstants.USER))
        securityContext.authentication = UsernamePasswordAuthenticationToken("user", "user", authorities)
        SecurityContextHolder.setContext(securityContext)

        assertThat(SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.USER)).isTrue()
        assertThat(SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)).isFalse()
    }

}
