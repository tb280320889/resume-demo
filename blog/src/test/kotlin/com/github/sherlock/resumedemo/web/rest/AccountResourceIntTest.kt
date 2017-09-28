package com.github.sherlock.resumedemo.web.rest

/**
 * Created by TangBin on 2017/9/28.
 */


import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.domain.Authority
import com.github.sherlock.resumedemo.domain.User
import com.github.sherlock.resumedemo.repository.AuthorityRepository
import com.github.sherlock.resumedemo.repository.UserRepository
import com.github.sherlock.resumedemo.security.AuthoritiesConstants
import com.github.sherlock.resumedemo.service.MailService
import com.github.sherlock.resumedemo.service.UserService
import com.github.sherlock.resumedemo.service.dto.UserDTO
import com.github.sherlock.resumedemo.web.rest.util.TestUtil
import com.github.sherlock.resumedemo.web.rest.vm.KeyAndPasswordVM
import com.github.sherlock.resumedemo.web.rest.vm.ManagedUserVM
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyObject
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
class AccountResourceIntTest {

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var authorityRepository: AuthorityRepository
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var httpMessageConverters: Array<HttpMessageConverter<*>>
    @Mock
    private lateinit var mockUserService: UserService
    @Mock
    private lateinit var mockMailService: MailService
    private lateinit var restUserMockMvc: MockMvc
    private lateinit var restMvc: MockMvc

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        doNothing().`when`(mockMailService).sendActivationEmail(anyObject())

        val accountResource = AccountResource(userRepository, userService, mockMailService)

        val accountUserMockResource = AccountResource(userRepository, mockUserService, mockMailService)

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource)
            .setMessageConverters(*httpMessageConverters)
            .build()
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build()
    }


    @Test
    @Throws(Exception::class)
    fun testNonAuthenticatedUser() {
        restUserMockMvc.perform(
            get("/api/authenticate")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().string(""))
    }

    @Test
    @Throws(Exception::class)
    fun testAuthenticatedUser() {
        restUserMockMvc.perform(
            get("/api/authenticate")
                .with(
                    { request ->
                        request.remoteUser = "test"
                        request
                    }
                )
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(content().string("test"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetExistingAccount() {
        val authorities = HashSet<Authority>()
        val authority = Authority()
        authority.name = (AuthoritiesConstants.ADMIN)
        authorities.add(authority)

        val user = User().apply {
            login = ("test")
            firstName = ("john")
            lastName = ("doe")
            email = ("john.doe@jhipster.com")
            imageUrl = ("http://placehold.it/50x50")
            langKey = ("en")
            this.authorities = (authorities)
        }
        `when`(mockUserService.getUserWithAuthorities()).thenReturn(user)

        restUserMockMvc.perform(
            get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("test"))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.imageUrl").value("http://placehold.it/50x50"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
    }

    @Test
    @Throws(Exception::class)
    fun testGetUnknownAccount() {
        `when`(mockUserService.getUserWithAuthorities()).thenReturn(null)

        restUserMockMvc.perform(
            get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isInternalServerError)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterValid() {
        val validUser = ManagedUserVM(
            null,
            "joe",
            "password",
            "Joe",
            "Shmoe",
            "joe@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )



        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser))
        )
            .andExpect(status().isCreated)

        val user = userRepository.findOneByLogin("joe")
        assertThat(user.isPresent).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidLogin() {
        val invalidUser = ManagedUserVM(
            null,
            "funky-log!n", //<-- invalid
            "password",
            "Funky",
            "One",
            "funky@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )



        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByEmail("funky@example.com")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidEmail() {
        val invalidUser = ManagedUserVM(
            null,
            "bob",
            "password",
            "Bob",
            "Green",
            "invalid", // <-- invalid
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )



        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidPassword() {
        val invalidUser = ManagedUserVM(
            null,
            "bob",
            "123", //with only 3 digits
            "Bob",
            "Green",
            "bob@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )



        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterNullPassword() {
        val invalidUser = ManagedUserVM(
            null,
            "bob",
            null, // invalid null password
            "Bob",
            "Green",
            "bob@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )




        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterDuplicateLogin() {
        // Good
        val validUser = ManagedUserVM(
            null,
            "alice",
            "password",
            "Alice",
            "Something",
            "alice@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )


        // Duplicate login, different email
        val duplicatedUser = ManagedUserVM(
            validUser.id,
            validUser.login,
            validUser.password,
            validUser.firstName,
            validUser.lastName,
            "alicejr@example.com",
            true,
            validUser.imageUrl,
            validUser.langKey,
            validUser.createdBy,
            validUser.createdDate,
            validUser.lastModifiedBy,
            validUser.lastModifiedDate,
            validUser.authorities
        )

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser))
        )
            .andExpect(status().isCreated)

        // Duplicate login
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser))
        )
            .andExpect(status().is4xxClientError)

        val userDup = userRepository.findOneByEmail("alicejr@example.com")
        assertThat(userDup.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterDuplicateEmail() {
        // Good
        val validUser = ManagedUserVM(
            null,
            "john",
            "password",
            "John",
            "Doe",
            "john@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.USER))
        )


        // Duplicate email, different login
        val duplicatedUser = ManagedUserVM(
            validUser.id,
            "johnjr",
            validUser.password,
            validUser.login,
            validUser.lastName,
            validUser.email,
            true,
            validUser.imageUrl,
            validUser.langKey,
            validUser.createdBy,
            validUser.createdDate,
            validUser.lastModifiedBy,
            validUser.lastModifiedDate,
            validUser.authorities
        )

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser))
        )
            .andExpect(status().isCreated)

        // Duplicate email
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser))
        )
            .andExpect(status().is4xxClientError)

        val userDup = userRepository.findOneByLogin("johnjr")
        assertThat(userDup.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterAdminIsIgnored() {
        val validUser = ManagedUserVM(
            null,
            "badguy",
            "password",
            "Bad",
            "Guy",
            "badguy@example.com",
            true,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.ADMIN))
        )



        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser))
        )
            .andExpect(status().isCreated)

        val userDup = userRepository.findOneByLogin("badguy")
        assertThat(userDup.isPresent).isTrue()
        assertThat(userDup.get().authorities).hasSize(1)
            .containsExactly(authorityRepository.findOne(AuthoritiesConstants.USER))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testActivateAccount() {
        val activationKey = "some activation key"
        var user = User()
        user.login = ("activate-account")
        user.email = ("activate-account@example.com")
        user.password = (RandomStringUtils.random(60))
        user.activated = (false)
        user.activationKey = (activationKey)

        userRepository.saveAndFlush(user)

        restMvc.perform(get("/api/activate?key={activationKey}", activationKey))
            .andExpect(status().isOk)

        user = userRepository.findOneByLogin(user.login).orElse(null)
        assertThat(user.activated).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testActivateAccountWithWrongKey() {
        restMvc.perform(get("/api/activate?key=wrongActivationKey"))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    @Throws(Exception::class)
    fun testSaveAccount() {
        val user = User()
        user.login = ("save-account")
        user.email = ("save-account@example.com")
        user.password = (RandomStringUtils.random(60))
        user.activated = (true)

        userRepository.saveAndFlush(user)

        val userDTO = UserDTO(
            null,
            "not-used",
            "firstname",
            "lastname",
            "save-account@example.com",
            null,
            false,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.ADMIN))
        )




        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin(user.login).orElse(null)
        assertThat(updatedUser.firstName).isEqualTo(userDTO.firstName)
        assertThat(updatedUser.lastName).isEqualTo(userDTO.lastName)
        assertThat(updatedUser.email).isEqualTo(userDTO.email)
        assertThat(updatedUser.langKey).isEqualTo(userDTO.langKey)
        assertThat(updatedUser.password).isEqualTo(user.password)
        assertThat(updatedUser.imageUrl).isEqualTo(userDTO.imageUrl)
        assertThat(updatedUser.activated).isEqualTo(true)
        assertThat(updatedUser.authorities).isEmpty()
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    @Throws(Exception::class)
    fun testSaveInvalidEmail() {
        val user = User()
        user.login = ("save-invalid-email")
        user.email = ("save-invalid-email@example.com")
        user.password = (RandomStringUtils.random(60))
        user.activated = (true)

        userRepository.saveAndFlush(user)

        val userDTO = UserDTO(
            null,
            "not-used",
            "firstname",
            "lastname",
            "invalid email",
            null,
            false,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.ADMIN))
        )



        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isBadRequest)

        assertThat(userRepository.findOneByEmail("invalid email")).isNotPresent
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    @Throws(Exception::class)
    fun testSaveExistingEmail() {
        val user = User()
        user.login = ("save-existing-email")
        user.email = ("save-existing-email@example.com")
        user.password = (RandomStringUtils.random(60))
        user.activated = (true)

        userRepository.saveAndFlush(user)

        val anotherUser = User()
        anotherUser.login = ("save-existing-email2")
        anotherUser.email = ("save-existing-email2@example.com")
        anotherUser.password = (RandomStringUtils.random(60))
        anotherUser.activated = (true)

        userRepository.saveAndFlush(anotherUser)

        val userDTO = UserDTO(
            null,
            "not-used",
            "firstname",
            "lastname",
            "save-existing-email2@example.com",
            null,
            false,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.ADMIN))
        )




        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("save-existing-email").orElse(null)
        assertThat(updatedUser.email).isEqualTo("save-existing-email@example.com")
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    @Throws(Exception::class)
    fun testSaveExistingEmailAndLogin() {
        val user = User()
        user.login = ("save-existing-email-and-login")
        user.email = ("save-existing-email-and-login@example.com")
        user.password = (RandomStringUtils.random(60))
        user.activated = (true)

        userRepository.saveAndFlush(user)

        val userDTO = UserDTO(
            null, // id
            "not-used",
            "firstname",
            "lastname",
            "save-existing-email-and-login@example.com",
            null,
            false,
            "http://placehold.it/50x50",
            "en",
            null,
            null,
            null,
            HashSet(listOf(AuthoritiesConstants.ADMIN))
        )




        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElse(null)
        assertThat(updatedUser.email).isEqualTo("save-existing-email-and-login@example.com")
    }

    @Test
    @Transactional
    @WithMockUser("change-password")
    @Throws(Exception::class)
    fun testChangePassword() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.login = ("change-password")
        user.email = ("change-password@example.com")
        userRepository.saveAndFlush(user)

        restMvc.perform(post("/api/account/change-password").content("new password"))
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin("change-password").orElse(null)
        assertThat(passwordEncoder.matches("new password", updatedUser.password)).isTrue()
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    @Throws(Exception::class)
    fun testChangePasswordTooSmall() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.login = ("change-password-too-small")
        user.email = ("change-password-too-small@example.com")
        userRepository.saveAndFlush(user)

        restMvc.perform(post("/api/account/change-password").content("new"))
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-too-small").orElse(null)
        assertThat(updatedUser.password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    @Throws(Exception::class)
    fun testChangePasswordTooLong() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.login = ("change-password-too-long")
        user.email = ("change-password-too-long@example.com")
        userRepository.saveAndFlush(user)

        restMvc.perform(post("/api/account/change-password").content(RandomStringUtils.random(101)))
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-too-long").orElse(null)
        assertThat(updatedUser.password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    @Throws(Exception::class)
    fun testChangePasswordEmpty() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.login = ("change-password-empty")
        user.email = ("change-password-empty@example.com")
        userRepository.saveAndFlush(user)

        restMvc.perform(post("/api/account/change-password").content(RandomStringUtils.random(0)))
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-empty").orElse(null)
        assertThat(updatedUser.password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRequestPasswordReset() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.activated = (true)
        user.login = ("password-reset")
        user.email = ("password-reset@example.com")
        userRepository.saveAndFlush(user)

        restMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset@example.com")
        )
            .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testRequestPasswordResetWrongEmail() {
        restMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset-wrong-email@example.com")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordReset() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.login = ("finish-password-reset")
        user.email = ("finish-password-reset@example.com")
        user.resetDate = (Instant.now().plusSeconds(60))
        user.resetKey = ("reset key")
        userRepository.saveAndFlush(user)

        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = (user.resetKey)
        keyAndPassword.newPassword = ("new password")

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin(user.login).orElse(null)
        assertThat(passwordEncoder.matches(keyAndPassword.newPassword, updatedUser.password)).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordResetTooSmall() {
        val user = User()
        user.password = (RandomStringUtils.random(60))
        user.login = ("finish-password-reset-too-small")
        user.email = ("finish-password-reset-too-small@example.com")
        user.resetDate = (Instant.now().plusSeconds(60))
        user.resetKey = ("reset key too small")
        userRepository.saveAndFlush(user)

        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = (user.resetKey)
        keyAndPassword.newPassword = ("foo")

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin(user.login).orElse(null)
        assertThat(passwordEncoder.matches(keyAndPassword.newPassword, updatedUser.password)).isFalse()
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordResetWrongKey() {
        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = ("wrong reset key")
        keyAndPassword.newPassword = ("new password")

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
        )
            .andExpect(status().isInternalServerError)
    }

}
