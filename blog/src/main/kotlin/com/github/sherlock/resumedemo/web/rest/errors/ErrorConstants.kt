package com.github.sherlock.resumedemo.web.rest.errors

import java.net.URI

/**
 * Created by TangBin on 2017/9/28.
 */

object ErrorConstants {

    const val ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure"
    const val ERR_VALIDATION = "error.validation"
    val DEFAULT_TYPE = URI.create("http://www.jhipster.tech/problem/problem-with-message")
    val CONSTRAINT_VIOLATION_TYPE = URI.create("http://www.jhipster.tech/problem/contraint-violation")
    val PARAMETERIZED_TYPE = URI.create("http://www.jhipster.tech/problem/parameterized")

}
