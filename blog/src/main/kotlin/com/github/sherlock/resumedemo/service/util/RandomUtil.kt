package com.github.sherlock.resumedemo.service.util

import org.apache.commons.lang3.RandomStringUtils

/**
 * Created by TangBin on 2017/9/27.
 *
 * Utility class for generating random Strings.
 */

object RandomUtil {
    private val DEF_COUNT = 20

    /**
     * Generate a password.
     *
     * @return the generated password
     */
    fun generatePassword(): String {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT)
    }

    /**
     * Generate an activation key.
     *
     * @return the generated activation key
     */
    fun generateActivationKey(): String {
        return RandomStringUtils.randomNumeric(DEF_COUNT)
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key
     */
    fun generateResetKey(): String {
        return RandomStringUtils.randomNumeric(DEF_COUNT)
    }

}
