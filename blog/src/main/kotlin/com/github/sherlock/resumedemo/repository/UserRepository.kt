package com.github.sherlock.resumedemo.repository

import com.github.sherlock.resumedemo.domain.User
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.*

/**
 * Created by TangBin on 2017/9/27.
 */

interface UserRepository : JpaRepository<User, Long> {

  fun findOneByActivationKey(activationKey: String): Optional<User>

  fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<User>

  fun findOneByResetKey(resetKey: String): Optional<User>

  fun findOneByEmail(email: String): Optional<User>

  fun findOneByLogin(login: String): Optional<User>

  @EntityGraph(attributePaths = arrayOf("authorities"))
  fun findOneWithAuthoritiesById(id: Long?): User

  @EntityGraph(attributePaths = arrayOf("authorities"))
  @Cacheable(cacheNames = arrayOf("users"))
  fun findOneWithAuthoritiesByLogin(login: String): Optional<User>

  fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>
}

