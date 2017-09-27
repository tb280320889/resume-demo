package com.github.sherlock.resumedemo.service

import com.github.sherlock.resumedemo.config.ConstantsKT
import com.github.sherlock.resumedemo.domain.Authority
import com.github.sherlock.resumedemo.domain.User
import com.github.sherlock.resumedemo.repository.AuthorityRepository
import com.github.sherlock.resumedemo.repository.UserRepository
import com.github.sherlock.resumedemo.security.AuthoritiesConstants
import com.github.sherlock.resumedemo.security.util.SecurityUtils
import com.github.sherlock.resumedemo.service.dto.UserDTO
import com.github.sherlock.resumedemo.service.util.RandomUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by TangBin on 2017/9/27.
 *
 * Service class for managing users.
 */
@Service
@Transactional
class UserService constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val socialService: SocialService,
    private val authorityRepository: AuthorityRepository,
    private val cacheManager: CacheManager
) {
  private val log = LoggerFactory.getLogger(UserService::class.java)


  fun activateRegistration(key: String): Optional<User> {
    log.debug("Activating user for activation key {}", key)
    return userRepository.findOneByActivationKey(key)
        .map { user ->
          // activate given user for the registration key.
          user.activated = (true)
          user.activationKey = (null)
          cacheManager.getCache("users").evict(user.login)
          log.debug("Activated user: {}", user)
          user
        }
  }

  fun completePasswordReset(newPassword: String, key: String): Optional<User> {
    log.debug("Reset user password for reset key {}", key)

    return userRepository.findOneByResetKey(key)
        .filter { user -> user.resetDate!!.isAfter(Instant.now().minusSeconds(86400)) }
        .map { user ->
          user.password = (passwordEncoder.encode(newPassword))
          user.resetKey = (null)
          user.resetDate = (null)
          cacheManager.getCache("users").evict(user.login)
          user
        }
  }

  fun requestPasswordReset(mail: String): Optional<User> {
    return userRepository.findOneByEmail(mail)
        .filter { it.activated }
        .map { user ->
          user.resetKey = (RandomUtil.generateResetKey())
          user.resetDate = (Instant.now())
          cacheManager.getCache("users").evict(user.login)
          user
        }
  }

  fun createUser(
      login: String, password: String, firstName: String, lastName: String, email: String,
      imageUrl: String, langKey: String
  ): User {
    val authority = authorityRepository.findOne(AuthoritiesConstants.USER)
    val authorities = HashSet<Authority>()
    val encryptedPassword = passwordEncoder.encode(password)

    val newUser = User().apply {
      this.login = (login)
      // new user gets initially a generated password
      this.password = (encryptedPassword)
      this.firstName = (firstName)
      this.lastName = (lastName)
      this.email = (email)
      this.imageUrl = (imageUrl)
      this.langKey = (langKey)
      // new user is not active
      this.activated = (false)
      // new user gets registration key
      this.activationKey = (RandomUtil.generateActivationKey())
      authorities.add(authority)
      this.authorities = (authorities)
    }

    userRepository.save(newUser)
    log.debug("Created Information for User: {}", newUser)
    return newUser
  }

  fun createUser(userDTO: UserDTO): User {
    val user = User().apply {

      login = userDTO.login
      firstName = userDTO.firstName
      lastName = userDTO.lastName
      email = userDTO.email
      imageUrl = userDTO.imageUrl
      langKey = if (userDTO.langKey == null) {
        "en" // default language
      } else {
        userDTO.langKey
      }
      if (userDTO.authorities != null) {
        val authorities = HashSet<Authority>()
        userDTO.authorities!!.forEach { authority -> authorities.add(authorityRepository.findOne(authority)) }
        this.authorities = authorities
      }
      val encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword())
      password = encryptedPassword
      resetKey = RandomUtil.generateResetKey()
      resetDate = Instant.now()
      activated = true
    }
    userRepository.save(user)
    log.debug("Created Information for User: {}", user)
    return user
  }

  /**
   * Update basic information (first name, last name, email, language) for the current user.
   *
   * @param firstName first name of user
   * @param lastName last name of user
   * @param email email id of user
   * @param langKey language key
   * @param imageUrl image URL of user
   */
  fun updateUser(firstName: String, lastName: String, email: String, langKey: String, imageUrl: String) {
    userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent { user ->
      user.firstName = firstName
      user.lastName = lastName
      user.email = email
      user.langKey = langKey
      user.imageUrl = imageUrl
      cacheManager.getCache("users").evict(user.login)
      log.debug("Changed Information for User: {}", user)
    }
  }

  /**
   * Update all information for a specific user, and return the modified user.
   *
   * @param userDTO user to update
   * @return updated user
   */
  fun updateUser(userDTO: UserDTO): Optional<UserDTO> {
    return Optional.of(
        userRepository
            .findOne(userDTO.id)
    ).map { user ->
      with(user) {
        login = userDTO.login
        firstName = userDTO.firstName
        lastName = userDTO.lastName
        email = userDTO.email
        imageUrl = userDTO.imageUrl
        activated = userDTO.activated
        langKey = userDTO.langKey
        val managedAuthorities = HashSet<Authority>(authorities)
        managedAuthorities.clear()
        userDTO.authorities
            ?.map { authorityRepository.findOne(it) }
            ?.forEach { managedAuthorities.add(it) }
        cacheManager.getCache("users").evict(user.login)
        log.debug("Changed Information for User: {}", user)
      }
      user
    }.map { UserDTO(it) }
  }

  fun deleteUser(login: String) {
    userRepository.findOneByLogin(login).ifPresent { user ->
      socialService.deleteUserSocialConnection(user.login!!)
      userRepository.delete(user)
      cacheManager.getCache("users").evict(login)
      log.debug("Deleted User: {}", user)
    }
  }

  fun changePassword(password: String) {
    userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent { user ->
      val encryptedPassword = passwordEncoder.encode(password)
      user.password = (encryptedPassword)
      cacheManager.getCache("users").evict(user.login)
      log.debug("Changed password for User: {}", user)
    }
  }

  @Transactional(readOnly = true)
  fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
    return userRepository.findAllByLoginNot(pageable, ConstantsKT.ANONYMOUS_USER).map { UserDTO(it) }
  }

  @Transactional(readOnly = true)
  fun getUserWithAuthoritiesByLogin(login: String): Optional<User> {
    return userRepository.findOneWithAuthoritiesByLogin(login)
  }

  @Transactional(readOnly = true)
  fun getUserWithAuthorities(id: Long?): User {
    return userRepository.findOneWithAuthoritiesById(id)
  }

  @Transactional(readOnly = true)
  fun getUserWithAuthorities(): User {
    return userRepository.findOneWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin()).orElse(null)
  }

  /**
   * Not activated users should be automatically deleted after 3 days.
   *
   *
   * This is scheduled to get fired everyday, at 01:00 (am).
   */
  @Scheduled(cron = "0 0 1 * * ?")
  fun removeNotActivatedUsers() {
    val users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
    for (user in users) {
      log.debug("Deleting not activated user {}", user.login)
      userRepository.delete(user)
      cacheManager.getCache("users").evict(user.login)
    }
  }

  /**
   * @return a list of all the authorities
   */
  fun getAuthorities(): List<String> {
    return authorityRepository.findAll().map { it.name }.filterNotNull()
  }
}
