package com.github.sherlock.resumedemo.config


import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.liquibase.AsyncSpringLiquibase
import liquibase.integration.spring.SpringLiquibase
import org.h2.tools.Server
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.task.TaskExecutor
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Created by TangBin on 2017/9/28.
 */
@Configuration
@EnableJpaRepositories("com.github.sherlock.resumedemo.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
class DatabaseConfiguration constructor(
    private val env: Environment
) {

    private val log = LoggerFactory.getLogger(DatabaseConfiguration::class.java)


    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server
     * @throws SQLException if the server failed to start
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    @Throws(SQLException::class)
    fun h2TCPServer(): Server {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers")
    }

    @Bean
    fun liquibase(
        @Qualifier("taskExecutor") taskExecutor: TaskExecutor,
        dataSource: DataSource, liquibaseProperties: LiquibaseProperties
    ): SpringLiquibase {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start asynchronously
        val liquibase = AsyncSpringLiquibase(taskExecutor, env)
        liquibase.dataSource = dataSource
        liquibase.changeLog = "classpath:config/liquibase/master.xml"
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
            liquibase.setShouldRun(false)
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled)
            log.debug("Configuring Liquibase")
        }
        return liquibase
    }

}
