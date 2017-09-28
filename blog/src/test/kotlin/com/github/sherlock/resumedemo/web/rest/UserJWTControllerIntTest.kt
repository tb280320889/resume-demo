package com.github.sherlock.resumedemo.web.rest


import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.domain.User
import com.github.sherlock.resumedemo.repository.UserRepository
import com.github.sherlock.resumedemo.security.jwt.TokenProvider
import com.github.sherlock.resumedemo.web.rest.util.TestUtil
import com.github.sherlock.resumedemo.web.rest.vm.LoginVM
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

/**
 * Created by TangBin on 2017/9/28.
 *
 * Test class for the UserJWTController REST controller.
 *
 * @see UserJWTController
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
class UserJWTControllerIntTest {

    @Autowired
    private lateinit var tokenProvider: TokenProvider
    @Autowired
    private lateinit var authenticationManager: AuthenticationManager
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        val userJWTController = UserJWTController(tokenProvider, authenticationManager)
        this.mockMvc = MockMvcBuilders.standaloneSetup(userJWTController)
            .build()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorize() {
        val user = User().apply {
            login = ("user-jwt-controller")
            email = ("user-jwt-controller@example.com")
            activated = (true)
            password = (passwordEncoder.encode("test"))
        }

        userRepository.saveAndFlush(user)

        val login = LoginVM().apply {
            username = ("user-jwt-controller")
            password = ("test")
        }
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(login))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_token").isString)
            .andExpect(jsonPath("$.id_token").isNotEmpty)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorizeWithRememberMe() {
        val user = User().apply {
            login = ("user-jwt-controller-remember-me")
            email = ("user-jwt-controller-remember-me@example.com")
            activated = (true)
            password = (passwordEncoder.encode("test"))
        }

        userRepository.saveAndFlush(user)

        val login = LoginVM().apply {
            username = ("user-jwt-controller-remember-me")
            password = ("test")
            isRememberMe = (true)
        }
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(login))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_token").isString)
            .andExpect(jsonPath("$.id_token").isNotEmpty)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorizeFails() {
        val login = LoginVM().apply {
            username = ("wrong-user")
            password = ("wrong password")
        }
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(login))
        ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.id_token").doesNotExist())
    }
}
