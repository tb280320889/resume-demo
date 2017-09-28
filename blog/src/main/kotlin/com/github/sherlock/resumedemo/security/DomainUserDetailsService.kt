package com.github.sherlock.resumedemo.security

import com.github.sherlock.resumedemo.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Created by TangBin on 2017/9/28.
 *
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
class DomainUserDetailsService
constructor(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val log = LoggerFactory.getLogger(DomainUserDetailsService::class.java)

    @Transactional
    override fun loadUserByUsername(login: String): UserDetails {
        log.debug("Authenticating {}", login)
        val lowercaseLogin = login.toLowerCase(Locale.ENGLISH)
        val userFromDatabase = userRepository.findOneWithAuthoritiesByLogin(lowercaseLogin)
        return userFromDatabase.map { user ->
            if (!user.activated) {
                throw UserNotActivatedException("User $lowercaseLogin was not activated")
            }
            val grantedAuthorities = user.authorities
                .map { SimpleGrantedAuthority(it.name) }
            org.springframework.security.core.userdetails.User(
                lowercaseLogin,
                user.password,
                grantedAuthorities
            )
        }.orElseThrow {
            UsernameNotFoundException(
                "User " + lowercaseLogin + " was not found in the " +
                "database"
            )
        }
    }
}
