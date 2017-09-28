package com.github.sherlock.resumedemo.config

import com.hazelcast.config.Config
import com.hazelcast.config.EvictionPolicy
import com.hazelcast.config.MapConfig
import com.hazelcast.config.MaxSizeConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.JHipsterProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.serviceregistry.Registration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import javax.annotation.PreDestroy

/**
 * Created by TangBin on 2017/9/28.
 */
@Configuration
@EnableCaching
@AutoConfigureAfter(value = *arrayOf(MetricsConfiguration::class))
@AutoConfigureBefore(value = *arrayOf(WebConfigurer::class, DatabaseConfiguration::class))
class CacheConfiguration constructor(
    private val serverProperties: ServerProperties,
    private val discoveryClient: DiscoveryClient,
    private val env: Environment
) {

    private val log = LoggerFactory.getLogger(CacheConfiguration::class.java)
    var registration: Registration? = null
        @Autowired(required = false) set

    @PreDestroy
    fun destroy() {
        log.info("Closing Cache Manager")
        Hazelcast.shutdownAll()
    }

    @Bean
    fun cacheManager(hazelcastInstance: HazelcastInstance): CacheManager {
        log.debug("Starting HazelcastCacheManager")
        return HazelcastCacheManager(hazelcastInstance)
    }

    @Bean
    fun hazelcastInstance(jHipsterProperties: JHipsterProperties): HazelcastInstance {
        log.debug("Configuring Hazelcast")
        val hazelCastInstance = Hazelcast.getHazelcastInstanceByName("blog")
        if (hazelCastInstance != null) {
            log.debug("Hazelcast already initialized")
            return hazelCastInstance
        }
        val config = Config()
        config.instanceName = "blog"
        config.networkConfig.join.multicastConfig.isEnabled = false
        if (this.registration == null) {
            log.warn("No discovery service is set up, Hazelcast cannot create a cluster.")
        } else {
            // The serviceId is by default the application's name, see Spring Boot's eureka.instance.appname property
            val serviceId = registration!!.serviceId
            log.debug("Configuring Hazelcast clustering for instanceId: {}", serviceId)
            // In development, everything goes through 127.0.0.1, with a different port
            if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)) {
                log.debug("Application is running with the \"dev\" profile, Hazelcast " + "cluster will only work with localhost instances")

                System.setProperty("hazelcast.local.localAddress", "127.0.0.1")
                config.networkConfig.port = serverProperties.port!! + 5701
                config.networkConfig.join.tcpIpConfig.isEnabled = true
                for (instance in discoveryClient.getInstances(serviceId)) {
                    val clusterMember = "127.0.0.1:" + (instance.port + 5701)
                    log.debug("Adding Hazelcast (dev) cluster member " + clusterMember)
                    config.networkConfig.join.tcpIpConfig.addMember(clusterMember)
                }
            } else { // Production configuration, one host per instance all using port 5701
                config.networkConfig.port = 5701
                config.networkConfig.join.tcpIpConfig.isEnabled = true
                for (instance in discoveryClient.getInstances(serviceId)) {
                    val clusterMember = instance.host + ":5701"
                    log.debug("Adding Hazelcast (prod) cluster member " + clusterMember)
                    config.networkConfig.join.tcpIpConfig.addMember(clusterMember)
                }
            }
        }
        config.mapConfigs.put("default", initializeDefaultMapConfig())
        config.mapConfigs.put("com.github.sherlock.demo.resume.domain.*", initializeDomainMapConfig(jHipsterProperties))
        return Hazelcast.newHazelcastInstance(config)
    }

    private fun initializeDefaultMapConfig(): MapConfig {
        val mapConfig = MapConfig()

        /*
            Number of backups. If 1 is set as the backup-count for example,
            then all entries of the map will be copied to another JVM for
            fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
         */
        mapConfig.backupCount = 0

        /*
            Valid values are:
            NONE (no eviction),
            LRU (Least Recently Used),
            LFU (Least Frequently Used).
            NONE is the default.
         */
        mapConfig.evictionPolicy = EvictionPolicy.LRU

        /*
            Maximum size of the map. When max size is reached,
            map is evicted based on the policy defined.
            Any integer between 0 and Integer.MAX_VALUE. 0 means
            Integer.MAX_VALUE. Default is 0.
         */
        mapConfig.maxSizeConfig = MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE)

        return mapConfig
    }

    private fun initializeDomainMapConfig(jHipsterProperties: JHipsterProperties): MapConfig {
        val mapConfig = MapConfig()
        mapConfig.timeToLiveSeconds = jHipsterProperties.cache.hazelcast.timeToLiveSeconds
        return mapConfig
    }

}
