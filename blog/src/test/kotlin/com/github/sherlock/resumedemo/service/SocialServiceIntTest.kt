package com.github.sherlock.resumedemo.service

import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.domain.User
import com.github.sherlock.resumedemo.repository.AuthorityRepository
import com.github.sherlock.resumedemo.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionKey
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.UserProfile
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 * Created by TangBin on 2017/9/27.
 */


@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
@Transactional
class SocialServiceIntTest {

    @Autowired
    private val authorityRepository: AuthorityRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Mock
    private val mockMailService: MailService? = null

    @Mock
    private val mockUsersConnectionRepository: UsersConnectionRepository? = null

    @Mock
    private val mockConnectionRepository: ConnectionRepository? = null

    private var socialService: SocialService? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        doNothing().`when`(mockMailService)!!.sendSocialRegistrationValidationEmail(Matchers.anyObject(), Matchers.anyString())
        doNothing().`when`(mockConnectionRepository)!!.addConnection(Matchers.anyObject())
        `when`<ConnectionRepository>(mockUsersConnectionRepository!!.createConnectionRepository(Matchers.anyString())).thenReturn(mockConnectionRepository)

        socialService = SocialService(
            mockUsersConnectionRepository, authorityRepository!!,
            passwordEncoder!!, userRepository!!, mockMailService!!
        )
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteUserSocialConnection() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )
        socialService!!.createSocialUser(connection, "fr")
        val connectionsByProviderId = LinkedMultiValueMap<String, Connection<*>>()
        connectionsByProviderId.put("PROVIDER", null)
        `when`<MultiValueMap<String, Connection<*>>>(mockConnectionRepository!!.findAllConnections()).thenReturn(connectionsByProviderId)

        // Exercise
        socialService!!.deleteUserSocialConnection("@LOGIN")

        // Verify
        verify<ConnectionRepository>(mockConnectionRepository, times(1)).removeConnections("PROVIDER")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSocialUserShouldThrowExceptionIfConnectionIsNull() {
        // Exercise
        socialService!!.createSocialUser(null, "fr")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSocialUserShouldThrowExceptionIfConnectionHasNoEmailAndNoLogin() {
        // Setup
        val connection = createConnection(
            "",
            "",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSocialUserShouldThrowExceptionIfConnectionHasNoEmailAndLoginAlreadyExist() {
        // Setup
        val user = createExistingUser(
            "@LOGIN",
            "mail@mail.com",
            "OTHER_FIRST_NAME",
            "OTHER_LAST_NAME",
            "OTHER_IMAGE_URL"
        )
        val connection = createConnection(
            "@LOGIN",
            "",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        try {
            // Exercise
            socialService!!.createSocialUser(connection, "fr")
        }
        finally {
            // Teardown
            userRepository!!.delete(user)
        }
    }

    @Test
    fun testCreateSocialUserShouldCreateUserIfNotExist() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        // Verify
        val user = userRepository!!.findOneByEmail("mail@mail.com")
        assertThat(user).isPresent

        // Teardown
        userRepository.delete(user.get())
    }

    @Test
    fun testCreateSocialUserShouldCreateUserWithSocialInformation() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        val user = userRepository!!.findOneByEmail("mail@mail.com").get()
        assertThat(user.firstName).isEqualTo("FIRST_NAME")
        assertThat(user.lastName).isEqualTo("LAST_NAME")
        assertThat(user.imageUrl).isEqualTo("IMAGE_URL")

        // Teardown
        userRepository.delete(user)
    }

    @Test
    fun testCreateSocialUserShouldCreateActivatedUserWithRoleUserAndPassword() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        val user = userRepository!!.findOneByEmail("mail@mail.com").get()
        assertThat(user.activated).isEqualTo(true)
        assertThat(user.password).isNotEmpty()
        val userAuthority = authorityRepository!!.findOne("ROLE_USER")
        assertThat(user.authorities).containsExactly(userAuthority)

        // Teardown
        userRepository.delete(user)
    }

    @Test
    fun testCreateSocialUserShouldCreateUserWithExactLangKey() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        val user = userRepository!!.findOneByEmail("mail@mail.com").get()
        assertThat(user.langKey).isEqualTo("fr")

        // Teardown
        userRepository.delete(user)
    }

    @Test
    fun testCreateSocialUserShouldCreateUserWithLoginSameAsEmailIfNotTwitter() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER_OTHER_THAN_TWITTER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        val user = userRepository!!.findOneByEmail("mail@mail.com").get()
        assertThat(user.login).isEqualTo("mail@mail.com")

        // Teardown
        userRepository.delete(user)
    }

    @Test
    fun testCreateSocialUserShouldCreateUserWithSocialLoginWhenIsTwitter() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "twitter"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        val user = userRepository!!.findOneByEmail("mail@mail.com").get()
        assertThat(user.login).isEqualToIgnoringCase("@LOGIN")

        // Teardown
        userRepository.delete(user)
    }

    @Test
    fun testCreateSocialUserShouldCreateSocialConnection() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        verify<ConnectionRepository>(mockConnectionRepository, times(1)).addConnection(connection)

        // Teardown
        val userToDelete = userRepository!!.findOneByEmail("mail@mail.com").get()
        userRepository.delete(userToDelete)
    }

    @Test
    fun testCreateSocialUserShouldNotCreateUserIfEmailAlreadyExist() {
        // Setup
        createExistingUser(
            "@OTHER_LOGIN",
            "mail@mail.com",
            "OTHER_FIRST_NAME",
            "OTHER_LAST_NAME",
            "OTHER_IMAGE_URL"
        )
        val initialUserCount = userRepository!!.count()
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        assertThat(userRepository.count()).isEqualTo(initialUserCount)

        // Teardown
        val userToDelete = userRepository.findOneByEmail("mail@mail.com").get()
        userRepository.delete(userToDelete)
    }

    @Test
    fun testCreateSocialUserShouldNotChangeUserIfEmailAlreadyExist() {
        // Setup
        createExistingUser(
            "@OTHER_LOGIN",
            "mail@mail.com",
            "OTHER_FIRST_NAME",
            "OTHER_LAST_NAME",
            "OTHER_IMAGE_URL"
        )
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        val userToVerify = userRepository!!.findOneByEmail("mail@mail.com").get()
        assertThat(userToVerify.login).isEqualTo("@other_login")
        assertThat(userToVerify.firstName).isEqualTo("OTHER_FIRST_NAME")
        assertThat(userToVerify.lastName).isEqualTo("OTHER_LAST_NAME")
        assertThat(userToVerify.imageUrl).isEqualTo("OTHER_IMAGE_URL")
        // Teardown
        userRepository.delete(userToVerify)
    }

    @Test
    fun testCreateSocialUserShouldSendRegistrationValidationEmail() {
        // Setup
        val connection = createConnection(
            "@LOGIN",
            "mail@mail.com",
            "FIRST_NAME",
            "LAST_NAME",
            "IMAGE_URL",
            "PROVIDER"
        )

        // Exercise
        socialService!!.createSocialUser(connection, "fr")

        //Verify
        verify(mockMailService, times(1))!!.sendSocialRegistrationValidationEmail(Matchers.anyObject(), Matchers.anyString())

        // Teardown
        val userToDelete = userRepository!!.findOneByEmail("mail@mail.com").get()
        userRepository.delete(userToDelete)
    }

    private fun createConnection(
        login: String,
        email: String,
        firstName: String,
        lastName: String,
        imageUrl: String,
        providerId: String
    ): Connection<*> {
        val userProfile = mock<UserProfile>(UserProfile::class.java)
        `when`<String>(userProfile.email).thenReturn(email)
        `when`<String>(userProfile.username).thenReturn(login)
        `when`<String>(userProfile.firstName).thenReturn(firstName)
        `when`<String>(userProfile.lastName).thenReturn(lastName)

        val connection = mock<Connection<*>>(Connection::class.java)
        val key = ConnectionKey(providerId, "PROVIDER_USER_ID")
        `when`<UserProfile>(connection.fetchUserProfile()).thenReturn(userProfile)
        `when`<ConnectionKey>(connection.key).thenReturn(key)
        `when`<String>(connection.imageUrl).thenReturn(imageUrl)

        return connection
    }

    private fun createExistingUser(
        login: String,
        email: String,
        firstName: String,
        lastName: String,
        imageUrl: String
    ): User {
        val user = User()
        user.login = (login)
        user.password = (passwordEncoder!!.encode("password"))
        user.email = (email)
        user.firstName = (firstName)
        user.lastName = (lastName)
        user.imageUrl = (imageUrl)
        return userRepository!!.saveAndFlush(user)
    }
}
