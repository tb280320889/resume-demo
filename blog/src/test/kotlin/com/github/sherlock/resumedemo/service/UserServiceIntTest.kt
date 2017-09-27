package com.github.sherlock.resumedemo.service

import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.config.ConstantsKT
import com.github.sherlock.resumedemo.repository.UserRepository
import com.github.sherlock.resumedemo.service.util.RandomUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Created by TangBin on 2017/9/27.
 */

@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
@Transactional
class UserServiceIntTest {

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var userService: UserService

  @Test
  fun assertThatUserMustExistToResetPassword() {
    var maybeUser = userService.requestPasswordReset("john.doe@localhost")
    assertThat(maybeUser.isPresent).isFalse()

    maybeUser = userService.requestPasswordReset("admin@localhost")
    assertThat(maybeUser.isPresent).isTrue()

    assertThat(maybeUser.get().email).isEqualTo("admin@localhost")
    assertThat(maybeUser.get().resetDate).isNotNull()
    assertThat(maybeUser.get().resetKey).isNotNull()
  }

  @Test
  fun assertThatOnlyActivatedUserCanRequestPasswordReset() {
    val user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US")
    val maybeUser = userService.requestPasswordReset("john.doe@localhost")
    assertThat(maybeUser.isPresent).isFalse()
    userRepository.delete(user)
  }

  @Test
  fun assertThatResetKeyMustNotBeOlderThan24Hours() {
    val user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US")

    val daysAgo = Instant.now().minus(25, ChronoUnit.HOURS)
    val resetKey = RandomUtil.generateResetKey()
    user.activated = true
    user.resetDate = daysAgo
    user.resetKey = resetKey

    val save = userRepository.save(user)

    val maybeUser = userService.completePasswordReset("johndoe2", save.resetKey!!)

    assertThat(maybeUser.isPresent).isFalse()

    userRepository.delete(user)
  }

  @Test
  fun assertThatResetKeyMustBeValid() {
    val user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US")

    val daysAgo = Instant.now().minus(25, ChronoUnit.HOURS)
    user.activated = (true)
    user.resetDate = (daysAgo)
    user.resetKey = ("1234")
    val save = userRepository.save(user)
    val maybeUser = userService.completePasswordReset("johndoe2", save.resetKey!!)
    assertThat(maybeUser.isPresent).isFalse()
    userRepository.delete(user)
  }

  @Test
  fun assertThatUserCanResetPassword() {
    val user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US")
    val oldPassword = user.password
    val daysAgo = Instant.now().minus(2, ChronoUnit.HOURS)
    val resetKey = RandomUtil.generateResetKey()
    user.activated = (true)
    user.resetDate = (daysAgo)
    user.resetKey = (resetKey)
    val save = userRepository.save(user)
    val maybeUser = userService.completePasswordReset("johndoe2", save.resetKey!!)
    assertThat(maybeUser.isPresent).isTrue()
    assertThat(maybeUser.get().resetDate).isNull()
    assertThat(maybeUser.get().resetKey).isNull()
    assertThat(maybeUser.get().password).isNotEqualTo(oldPassword)

    userRepository.delete(user)
  }

  @Test
  fun testFindNotActivatedUsersByCreationDateBefore() {
    userService.removeNotActivatedUsers()
    val now = Instant.now()
    val users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS))
    assertThat(users).isEmpty()
  }

  @Test
  fun assertThatAnonymousUserIsNotGet() {
    val pageable = PageRequest(0, userRepository.count().toInt())
    val allManagedUsers = userService.getAllManagedUsers(pageable)
    assertThat(
        allManagedUsers.content.stream()
            .noneMatch { user -> ConstantsKT.ANONYMOUS_USER == user.login }
    )
        .isTrue()
  }

  @Test
  fun testRemoveNotActivatedUsers() {
    val user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US")
    user.activated = (false)
    user.createdDate = (Instant.now().minus(30, ChronoUnit.DAYS))
    userRepository.save(user)
    assertThat(userRepository.findOneByLogin("johndoe")).isPresent
    userService.removeNotActivatedUsers()
    assertThat(userRepository.findOneByLogin("johndoe")).isNotPresent
  }
}
