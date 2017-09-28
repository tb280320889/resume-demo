package com.github.sherlock.resumedemo.service

import com.github.sherlock.resumedemo.domain.Authority
import com.github.sherlock.resumedemo.domain.User
import com.github.sherlock.resumedemo.repository.AuthorityRepository
import com.github.sherlock.resumedemo.repository.UserRepository
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.social.connect.Connection
import org.springframework.social.connect.UserProfile
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by TangBin on 2017/9/27.
 */

@Service
class SocialService constructor(
    private val usersConnectionRepository: UsersConnectionRepository,
    private val authorityRepository: AuthorityRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val mailService: MailService
) {
    private val log = LoggerFactory.getLogger(SocialService::class.java)


    fun deleteUserSocialConnection(login: String) {
        val connectionRepository = usersConnectionRepository.createConnectionRepository(login)
        connectionRepository.findAllConnections().keys.stream()
            .forEach { providerId ->
                connectionRepository.removeConnections(providerId)
                log.debug("Delete user social connection providerId: {}", providerId)
            }
    }

    @Throws(IllegalArgumentException::class)
    fun createSocialUser(connection: Connection<*>?, langKey: String) {
        if (connection == null) {
            log.error("Cannot create social user because connection is null")
            throw IllegalArgumentException("Connection cannot be null")
        }
        val userProfile = connection.fetchUserProfile()
        val providerId = connection.key.providerId
        val imageUrl = connection.imageUrl
        val user = createUserIfNotExist(userProfile, langKey, providerId, imageUrl)
        createSocialConnection(user.login, connection)
        mailService.sendSocialRegistrationValidationEmail(user, providerId)
    }

    @Throws(IllegalArgumentException::class)
    private fun createUserIfNotExist(userProfile: UserProfile, langKey: String, providerId: String, imageUrl: String): User {
        val email = userProfile.email
        var userName = userProfile.username
        if (!StringUtils.isBlank(userName)) {
            userName = userName.toLowerCase(Locale.ENGLISH)
        }
        if (StringUtils.isBlank(email) && StringUtils.isBlank(userName)) {
            log.error("Cannot create social user because email and login are null")
            throw IllegalArgumentException("Email and login cannot be null")
        }
        if (StringUtils.isBlank(email) && userRepository.findOneByLogin(userName).isPresent) {
            log.error("Cannot create social user because email is null and login already exist, login -> {}", userName)
            throw IllegalArgumentException("Email cannot be null with an existing login")
        }
        if (!StringUtils.isBlank(email)) {
            val user = userRepository.findOneByEmail(email)
            if (user.isPresent) {
                log.info("User already exist associate the connection to this account")
                return user.get()
            }
        }

        val login = getLoginDependingOnProviderId(userProfile, providerId)
        val encryptedPassword = passwordEncoder.encode(RandomStringUtils.random(10))
        val authorities = HashSet<Authority>(1)
        authorities.add(authorityRepository.findOne("ROLE_USER"))

        val newUser = User().apply {
            this.login = login
            this.password = encryptedPassword
            this.firstName = userProfile.firstName
            this.lastName = userProfile.lastName
            this.email = email
            this.activated = true
            this.authorities = authorities
            this.langKey = langKey
            this.imageUrl = imageUrl
        }
        return userRepository.save(newUser)
    }

    /**
     * @return login if provider manage a login like Twitter or GitHub otherwise email address.
     * Because provider like Google or Facebook didn't provide login or login like "12099388847393"
     */
    private fun getLoginDependingOnProviderId(userProfile: UserProfile, providerId: String): String {
        return when (providerId) {
            "twitter" -> userProfile.username.toLowerCase()
            else      -> userProfile.email
        }
    }

    private fun createSocialConnection(login: String?, connection: Connection<*>) {
        val connectionRepository = usersConnectionRepository.createConnectionRepository(login)
        connectionRepository.addConnection(connection)
    }

}
