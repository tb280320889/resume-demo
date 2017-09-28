package com.github.sherlock.resumedemo.config

import io.github.jhipster.config.JHipsterConstants
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cloud.config.java.AbstractCloudConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

/**
 * Created by TangBin on 2017/9/28.
 */
@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_CLOUD)
class CloudDatabaseConfiguration : AbstractCloudConfig() {

    private val log = LoggerFactory.getLogger(CloudDatabaseConfiguration::class.java)

    @Bean
    fun dataSource(cacheManager: CacheManager): DataSource {
        log.info("Configuring JDBC datasource from a cloud provider")
        return connectionFactory().dataSource()
    }

}
