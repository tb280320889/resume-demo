package com.github.sherlock.resumedemo.config

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.boolex.OnMarkerEvaluator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggerContextListener
import ch.qos.logback.core.filter.EvaluatorFilter
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.FilterReply
import io.github.jhipster.config.JHipsterProperties
import net.logstash.logback.appender.LogstashTcpSocketAppender
import net.logstash.logback.encoder.LogstashEncoder
import net.logstash.logback.stacktrace.ShortenedThrowableConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean
import org.springframework.context.annotation.Configuration
import java.net.InetSocketAddress

/**
 * Created by TangBin on 2017/9/28.
 */
@Configuration
@ConditionalOnProperty("eureka.client.enabled")
class LoggingConfiguration
constructor(
    @Value(value = "\${spring.application.appName}") private val appName: String,

    @Value("\${server.port}") private val serverPort: String,

    private val eurekaInstanceConfigBean: EurekaInstanceConfigBean,

    @Value("\${info.project.version}") private val version: String,

    private val jHipsterProperties: JHipsterProperties

) {

    private val LOGSTASH_APPENDER_NAME = "LOGSTASH"
    private val ASYNC_LOGSTASH_APPENDER_NAME = "ASYNC_LOGSTASH"

    private val log = LoggerFactory.getLogger(LoggingConfiguration::class.java)

    private fun addContextListener(context: LoggerContext) {
        val loggerContextListener = LogbackLoggerContextListener()
        loggerContextListener.context = context
        context.addListener(loggerContextListener)
    }

    private fun addLogstashAppender(context: LoggerContext) {
        log.info("Initializing Logstash logging")

        val logstashAppender = LogstashTcpSocketAppender()
        logstashAppender.name = "LOGSTASH"
        logstashAppender.context = context
        val customFields = "{\"app_name\":\"$appName\",\"app_port\":\"$serverPort\"}"

        // More documentation is available at: https://github.com/logstash/logstash-logback-encoder
        val logstashEncoder = LogstashEncoder()
        // Set the Logstash appender config from JHipster properties
        logstashEncoder.customFields = customFields
        // Set the Logstash appender config from JHipster properties
        logstashAppender.addDestinations(InetSocketAddress(jHipsterProperties.logging.logstash.host, jHipsterProperties.logging.logstash.port))

        val throwableConverter = ShortenedThrowableConverter()
        throwableConverter.isRootCauseFirst = true
        logstashEncoder.throwableConverter = throwableConverter
        logstashEncoder.customFields = customFields

        logstashAppender.encoder = logstashEncoder
        logstashAppender.start()

        // Wrap the appender in an Async appender for performance
        val asyncLogstashAppender = AsyncAppender()
        asyncLogstashAppender.context = context
        asyncLogstashAppender.name = "ASYNC_LOGSTASH"
        asyncLogstashAppender.queueSize = jHipsterProperties.logging.logstash.queueSize
        asyncLogstashAppender.addAppender(logstashAppender)
        asyncLogstashAppender.start()

        context.getLogger("ROOT").addAppender(asyncLogstashAppender)
    }

    // Configure a log filter to remove "metrics" logs from all appenders except the "LOGSTASH" appender
    private fun setMetricsMarkerLogbackFilter(context: LoggerContext) {
        log.info("Filtering metrics logs from all appenders except the {} appender", LOGSTASH_APPENDER_NAME)
        val onMarkerMetricsEvaluator = OnMarkerEvaluator()
        onMarkerMetricsEvaluator.context = context
        onMarkerMetricsEvaluator.addMarker("metrics")
        onMarkerMetricsEvaluator.start()
        val metricsFilter = EvaluatorFilter<ILoggingEvent>()
        metricsFilter.context = context
        metricsFilter.setEvaluator(onMarkerMetricsEvaluator)
        metricsFilter.onMatch = FilterReply.DENY
        metricsFilter.start()

        context.loggerList
            .map { it.iteratorForAppenders() }
            .forEach { it ->
                while (it.hasNext()) {
                    val appender = it.next()
                    if (appender.name != ASYNC_LOGSTASH_APPENDER_NAME) {
                        log.debug("Filter metrics logs from the {} appender", appender.name)
                        appender.context = context
                        appender.addFilter(metricsFilter)
                        appender.start()
                    }
                }
            }
    }

    /**
     * Logback configuration is achieved by configuration file and API.
     * When configuration file change is detected, the configuration is reset.
     * This listener ensures that the programmatic configuration is also re-applied after reset.
     */
    internal inner class LogbackLoggerContextListener : ContextAwareBase(), LoggerContextListener {

        override fun isResetResistant(): Boolean {
            return true
        }

        override fun onStart(context: LoggerContext) {
            addLogstashAppender(context)
        }

        override fun onReset(context: LoggerContext) {
            addLogstashAppender(context)
        }

        override fun onStop(context: LoggerContext) {
            // Nothing to do.
        }

        override fun onLevelChange(logger: ch.qos.logback.classic.Logger, level: Level) {
            // Nothing to do.
        }
    }

}
