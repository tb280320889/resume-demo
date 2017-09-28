package com.github.sherlock.resumedemo.security

import com.github.sherlock.resumedemo.config.constants.ConstantsKT
import com.github.sherlock.resumedemo.security.util.SecurityUtils
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component

/**
 * Created by TangBin on 2017/9/28.
 *
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): String {
        val userName = SecurityUtils.getCurrentUserLogin()
        return userName ?: ConstantsKT.SYSTEM_ACCOUNT
    }
}
