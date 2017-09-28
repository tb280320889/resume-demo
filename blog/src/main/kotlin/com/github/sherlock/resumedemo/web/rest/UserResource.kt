package com.github.sherlock.resumedemo.web.rest

import com.codahale.metrics.annotation.Timed
import com.github.sherlock.resumedemo.config.constants.ConstantsKT
import com.github.sherlock.resumedemo.repository.UserRepository
import com.github.sherlock.resumedemo.security.AuthoritiesConstants
import com.github.sherlock.resumedemo.service.MailService
import com.github.sherlock.resumedemo.service.UserService
import com.github.sherlock.resumedemo.service.dto.UserDTO
import com.github.sherlock.resumedemo.web.rest.util.HeaderUtil
import com.github.sherlock.resumedemo.web.rest.util.PaginationUtil
import com.github.sherlock.resumedemo.web.rest.vm.ManagedUserVM
import io.github.jhipster.web.util.ResponseUtil
import io.swagger.annotations.ApiParam
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URISyntaxException
import javax.validation.Valid

/**
 * Created by TangBin on 2017/9/27.
 *
 * REST controller for managing users.
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
class UserResource
constructor(
    private val userRepository: UserRepository,
    private val mailService: MailService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(UserResource::class.java)

    private val ENTITY_NAME = "userManagement"


    /**
     * POST  /users  : Creates a new user.
     *
     *
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param managedUserVM the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/users")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    @Throws(URISyntaxException::class)
    fun createUser(@Valid @RequestBody managedUserVM: ManagedUserVM): ResponseEntity<*> {
        log.debug("REST request to save User : {}", managedUserVM)

        if (managedUserVM.id != null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new user cannot already have an ID"))
                .body<Any>(null)
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(managedUserVM.login?.toLowerCase()).isPresent) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "userexists", "Login already in use"))
                .body<Any>(null)
        } else if (userRepository.findOneByEmail(managedUserVM.email).isPresent) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "emailexists", "Email already in use"))
                .body<Any>(null)
        } else {
            val newUser = userService.createUser(managedUserVM)
            mailService.sendCreationEmail(newUser)
            return ResponseEntity.created(URI("/api/users/" + newUser.login))
                .headers(HeaderUtil.createAlert("userManagement.created", newUser.login))
                .body<Any>(newUser)
        }
    }

    /**
     * PUT  /users : Updates an existing User.
     *
     * @param managedUserVM the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user,
     * or with status 400 (Bad Request) if the login or email is already in use,
     * or with status 500 (Internal Server Error) if the user couldn't be updated
     */
    @PutMapping("/users")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    fun updateUser(@Valid @RequestBody managedUserVM: ManagedUserVM): ResponseEntity<UserDTO> {
        log.debug("REST request to update User : {}", managedUserVM)
        var existingUser = userRepository.findOneByEmail(managedUserVM.email)
        if (existingUser.isPresent && existingUser.get().id != (managedUserVM.id)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "emailexists", "Email already in use")).body(null)
        }
        existingUser = userRepository.findOneByLogin(managedUserVM.login?.toLowerCase())
        if (existingUser.isPresent && existingUser.get().id != managedUserVM.id) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "userexists", "Login already in use")).body(null)
        }
        val updatedUser = userService.updateUser(managedUserVM)

        return ResponseUtil.wrapOrNotFound(
            updatedUser,
            HeaderUtil.createAlert("userManagement.updated", managedUserVM.login)
        )
    }

    /**
     * GET  /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    fun getAllUsers(@ApiParam pageable: Pageable): ResponseEntity<List<UserDTO>> {
        val page = userService.getAllManagedUsers(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * @return a string list of the all of the roles
     */
    @GetMapping("/users/authorities")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    fun getAuthorities(): List<String> {
        return userService.getAuthorities()
    }

    /**
     * GET  /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + ConstantsKT.LOGIN_REGEX + "}")
    @Timed
    fun getUser(@PathVariable login: String): ResponseEntity<UserDTO> {
        log.debug("REST request to get User : {}", login)
        return ResponseUtil.wrapOrNotFound(
            userService.getUserWithAuthoritiesByLogin(login)
                .map { UserDTO(it) }
        )
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + ConstantsKT.LOGIN_REGEX + "}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    fun deleteUser(@PathVariable login: String): ResponseEntity<Void> {
        log.debug("REST request to delete User: {}", login)
        userService.deleteUser(login)
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login)).build()
    }

}
