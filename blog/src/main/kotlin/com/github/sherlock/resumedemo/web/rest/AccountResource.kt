package com.github.sherlock.resumedemo.web.rest

import com.codahale.metrics.annotation.Timed
import com.github.sherlock.resumedemo.repository.UserRepository
import com.github.sherlock.resumedemo.security.util.SecurityUtils
import com.github.sherlock.resumedemo.service.MailService
import com.github.sherlock.resumedemo.service.UserService
import com.github.sherlock.resumedemo.service.dto.UserDTO
import com.github.sherlock.resumedemo.web.rest.util.HeaderUtil
import com.github.sherlock.resumedemo.web.rest.vm.KeyAndPasswordVM
import com.github.sherlock.resumedemo.web.rest.vm.ManagedUserVM
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

/**
 * Created by TangBin on 2017/9/28.
 *
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
class AccountResource
constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val mailService: MailService
) {

    private val log = LoggerFactory.getLogger(AccountResource::class.java)
    private val CHECK_ERROR_MESSAGE = "Incorrect password"

    private fun checkPasswordLength(password: String?): Boolean {
        return !StringUtils.isEmpty(password) &&
               password!!.length >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
               password.length <= ManagedUserVM.PASSWORD_MAX_LENGTH
    }

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @return the ResponseEntity with status 201 (Created) if the user is registered or 400 (Bad Request) if the login or email is already in use
     */
    @PostMapping(path = arrayOf("/register"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE))
    @Timed
    fun registerAccount(@Valid @RequestBody managedUserVM: ManagedUserVM): ResponseEntity<*> {

        val textPlainHeaders = HttpHeaders()
        textPlainHeaders.contentType = MediaType.TEXT_PLAIN
        return if (!checkPasswordLength(managedUserVM.password)) {
            ResponseEntity(CHECK_ERROR_MESSAGE, HttpStatus.BAD_REQUEST)
        } else userRepository.findOneByLogin(managedUserVM.login?.toLowerCase())
            .map { ResponseEntity("login already in use", textPlainHeaders, HttpStatus.BAD_REQUEST) }
            .orElseGet {
                userRepository.findOneByEmail(managedUserVM.email)
                    .map { ResponseEntity("email address already in use", textPlainHeaders, HttpStatus.BAD_REQUEST) }
                    .orElseGet {
                        val user = userService
                            .createUser(
                                managedUserVM.login, managedUserVM.password,
                                managedUserVM.firstName, managedUserVM.lastName,
                                managedUserVM.email?.toLowerCase(), managedUserVM.imageUrl,
                                managedUserVM.langKey
                            )

                        mailService.sendActivationEmail(user)
                        ResponseEntity(HttpStatus.CREATED)
                    }
            }
    }


    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    fun activateAccount(@RequestParam(value = "key") key: String): ResponseEntity<String> {
        return userService.activateRegistration(key)
            .map { ResponseEntity<String>(HttpStatus.OK) }
            .orElse(ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR))
    }


    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    fun isAuthenticated(request: HttpServletRequest): String {
        log.debug("REST request to check if the current user is authenticated")
        return request.remoteUser
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    fun getAccount(): ResponseEntity<UserDTO> {
        return Optional.ofNullable(userService.getUserWithAuthorities())
            .map { ResponseEntity(UserDTO(it), HttpStatus.OK) }
            .orElse(ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR))
    }


    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal Server Error) if the user couldn't be updated
     */
    @PostMapping("/account")
    @Timed
    fun saveAccount(@Valid @RequestBody userDTO: UserDTO): ResponseEntity<*> {
        val userLogin = SecurityUtils.getCurrentUserLogin()
        val existingUser = userRepository.findOneByEmail(userDTO.email)
        return if (existingUser.isPresent && !StringUtils.equalsIgnoreCase(existingUser.get().login, userLogin)) {
            ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use")).body<Any>(null)
        } else userRepository
            .findOneByLogin(userLogin)
            .map {
                userService.updateUser(
                    userDTO.firstName, userDTO.lastName, userDTO.email,
                    userDTO.langKey, userDTO.imageUrl
                )
                ResponseEntity<Any>(HttpStatus.OK)
            }
            .orElseGet({ ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) })
    }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new password is not strong enough
     */
    @PostMapping(path = arrayOf("/account/change-password"), produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    @Timed
    fun changePassword(@RequestBody password: String): ResponseEntity<*> {
        if (!checkPasswordLength(password)) {
            return ResponseEntity(CHECK_ERROR_MESSAGE, HttpStatus.BAD_REQUEST)
        }
        userService.changePassword(password)
        return ResponseEntity<Any>(HttpStatus.OK)
    }


    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = arrayOf("/account/reset-password/init"), produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    @Timed
    fun requestPasswordReset(@RequestBody mail: String): ResponseEntity<*> {
        return userService.requestPasswordReset(mail)
            .map { user ->
                mailService.sendPasswordResetMail(user)
                ResponseEntity<Any>("email was sent", HttpStatus.OK)
            }.orElse(ResponseEntity("email address not registered", HttpStatus.BAD_REQUEST))
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset,
     * or status 400 (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = arrayOf("/account/reset-password/finish"), produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    @Timed
    fun finishPasswordReset(@RequestBody keyAndPassword: KeyAndPasswordVM): ResponseEntity<String> {
        return if (!checkPasswordLength(keyAndPassword.newPassword)) {
            ResponseEntity(CHECK_ERROR_MESSAGE, HttpStatus.BAD_REQUEST)
        } else userService.completePasswordReset(keyAndPassword.newPassword, keyAndPassword.key)
            .map { ResponseEntity<String>(HttpStatus.OK) }
            .orElse(ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR))
    }

}
