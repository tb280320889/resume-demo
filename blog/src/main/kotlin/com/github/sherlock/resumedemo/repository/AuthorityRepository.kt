package com.github.sherlock.resumedemo.repository

import com.github.sherlock.resumedemo.domain.Authority
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by TangBin on 2017/9/27.
 */

interface AuthorityRepository : JpaRepository<Authority, String>
