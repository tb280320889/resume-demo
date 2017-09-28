package com.github.sherlock.resumedemo.config

import com.github.sherlock.resumedemo.blog.aop.logging.LoggingAspect
import io.github.jhipster.config.JHipsterConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

/**
 * Created by TangBin on 2017/9/28.
 */

@Configuration
@EnableAspectJAutoProxy
class LoggingAspectConfiguration {

    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    fun loggingAspect(env: Environment): LoggingAspect {
        return LoggingAspect(env)
    }

}
