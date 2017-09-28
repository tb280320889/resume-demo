package com.github.sherlock.resumedemo.config.util

import io.github.jhipster.config.JHipsterConstants
import org.springframework.boot.SpringApplication
import org.springframework.core.env.Environment
import java.util.*

/**
 * Created by TangBin on 2017/9/28.
 */

object DefaultProfileUtil {

    private const val SPRING_PROFILE_DEFAULT = "spring.profiles.default"

    /**
     * Set a default to use when no profile is configured.
     *
     * @param app the Spring application
     */
    @JvmStatic
    fun addDefaultProfile(app: SpringApplication) {
        val defProperties = HashMap<String, Any>()
        /*
            * The default profile to use when no other profiles are defined
            * This cannot be set in the <code>application.yml</code> file.
            * See https://github.com/spring-projects/spring-boot/issues/1219
            */
        defProperties.put(SPRING_PROFILE_DEFAULT, JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
        app.setDefaultProperties(defProperties)
    }

    /**
     * Get the profiles that are applied else get default profiles.
     *
     * @param env spring environment
     * @return profiles
     */
    @JvmStatic
    fun getActiveProfiles(env: Environment): Array<String> {
        val profiles = env.activeProfiles
        return if (profiles.size == 0) {
            env.defaultProfiles
        } else profiles
    }

}
