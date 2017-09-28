package com.github.sherlock.resumedemo.web.rest.vm

import com.github.sherlock.resumedemo.config.constants.ConstantsKT
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * Created by TangBin on 2017/9/28.
 *
 * View Model object for storing a user's credentials.
 */
class LoginVM {

    @Pattern(regexp = ConstantsKT.LOGIN_REGEX)
    @NotNull
    @Size(min = 1, max = 50)
    var username: String? = null

    @NotNull
    @Size(min = ManagedUserVM.PASSWORD_MIN_LENGTH, max = ManagedUserVM.PASSWORD_MAX_LENGTH)
    var password: String? = null

    var isRememberMe: Boolean? = null

    override fun toString(): String {
        return "LoginVM{" +
               "username='" + username + '\'' +
               ", rememberMe=" + isRememberMe +
               '}'
    }
}
