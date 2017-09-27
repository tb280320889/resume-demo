package com.github.sherlock.resumedemo.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by TangBin on 2017/9/27.
 */

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
class ApplicationProperties
