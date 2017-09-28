package com.github.sherlock.resumedemo.config


import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.servlet.InstrumentedFilter
import com.codahale.metrics.servlets.MetricsServlet
import com.hazelcast.config.Config
import com.hazelcast.core.ClientService
import com.hazelcast.core.Cluster
import com.hazelcast.core.DistributedObject
import com.hazelcast.core.DistributedObjectListener
import com.hazelcast.core.Endpoint
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IAtomicLong
import com.hazelcast.core.IAtomicReference
import com.hazelcast.core.ICacheManager
import com.hazelcast.core.ICountDownLatch
import com.hazelcast.core.IExecutorService
import com.hazelcast.core.IList
import com.hazelcast.core.ILock
import com.hazelcast.core.IMap
import com.hazelcast.core.IQueue
import com.hazelcast.core.ISemaphore
import com.hazelcast.core.ISet
import com.hazelcast.core.ITopic
import com.hazelcast.core.IdGenerator
import com.hazelcast.core.LifecycleService
import com.hazelcast.core.MultiMap
import com.hazelcast.core.PartitionService
import com.hazelcast.core.ReplicatedMap
import com.hazelcast.durableexecutor.DurableExecutorService
import com.hazelcast.logging.LoggingService
import com.hazelcast.mapreduce.JobTracker
import com.hazelcast.quorum.QuorumService
import com.hazelcast.ringbuffer.Ringbuffer
import com.hazelcast.transaction.HazelcastXAResource
import com.hazelcast.transaction.TransactionContext
import com.hazelcast.transaction.TransactionException
import com.hazelcast.transaction.TransactionOptions
import com.hazelcast.transaction.TransactionalTask
import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.web.filter.CachingHttpHeadersFilter
import io.undertow.Undertow
import io.undertow.UndertowOptions
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.h2.server.web.WebServlet
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Matchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory
import org.springframework.http.HttpHeaders
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.web.MockServletContext
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.xnio.OptionMap
import java.util.*
import java.util.concurrent.ConcurrentMap
import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.FilterRegistration
import javax.servlet.MultipartConfigElement
import javax.servlet.Servlet
import javax.servlet.ServletException
import javax.servlet.ServletRegistration
import javax.servlet.ServletSecurityElement


/**
 * Created by TangBin on 2017/9/28.
 */
/**
 * Unit tests for the WebConfigurer class.
 *
 * @see WebConfigurer
 */
class WebConfigurerTest {

    private lateinit var webConfigurer: WebConfigurer

    private lateinit var servletContext: MockServletContext

    private lateinit var env: MockEnvironment

    private lateinit var props: JHipsterProperties

    private lateinit var metricRegistry: MetricRegistry

    @Before
    fun setup() {
        servletContext = spy<MockServletContext>(MockServletContext())
        doReturn(MockFilterRegistration())
            .`when`(servletContext)!!.addFilter(anyString(), any<Filter>(Filter::class.java))
        doReturn(MockServletRegistration())
            .`when`(servletContext)!!.addServlet(anyString(), any<Servlet>(Servlet::class.java))

        env = MockEnvironment()
        props = JHipsterProperties()

        webConfigurer = WebConfigurer(env, props, MockHazelcastInstance())
        metricRegistry = MetricRegistry()
        webConfigurer.setMetricRegistry(metricRegistry)
    }

    @Test
    @Throws(ServletException::class)
    fun testStartUpProdServletContext() {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        webConfigurer.onStartup(servletContext)

        assertThat(servletContext.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE)).isEqualTo(metricRegistry)
        assertThat(servletContext.getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry)
        verify<MockServletContext>(servletContext).addFilter(Matchers.eq("webappMetricsFilter"), any<InstrumentedFilter>(InstrumentedFilter::class.java))
        verify<MockServletContext>(servletContext).addServlet(Matchers.eq("metricsServlet"), any<MetricsServlet>(MetricsServlet::class.java))
        verify<MockServletContext>(servletContext).addFilter(Matchers.eq("cachingHttpHeadersFilter"), any<CachingHttpHeadersFilter>(CachingHttpHeadersFilter::class.java))
        verify<MockServletContext>(servletContext, never()).addServlet(Matchers.eq("H2Console"), any<WebServlet>(WebServlet::class.java))
    }

    @Test
    @Throws(ServletException::class)
    fun testStartUpDevServletContext() {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
        webConfigurer.onStartup(servletContext)

        assertThat(servletContext.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE)).isEqualTo(metricRegistry)
        assertThat(servletContext.getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry)
        verify<MockServletContext>(servletContext).addFilter(Matchers.eq("webappMetricsFilter"), any<InstrumentedFilter>(InstrumentedFilter::class.java))
        verify<MockServletContext>(servletContext).addServlet(Matchers.eq("metricsServlet"), any<MetricsServlet>(MetricsServlet::class.java))
        verify<MockServletContext>(servletContext, never()).addFilter(Matchers.eq("cachingHttpHeadersFilter"), any<CachingHttpHeadersFilter>(CachingHttpHeadersFilter::class.java))
        verify<MockServletContext>(servletContext).addServlet(Matchers.eq("H2Console"), any<WebServlet>(WebServlet::class.java))
    }

    @Test
    fun testCustomizeServletContainer() {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        val container = UndertowEmbeddedServletContainerFactory()
        webConfigurer.customize(container)
        assertThat(container.mimeMappings.get("abs")).isEqualTo("audio/x-mpeg")
        assertThat(container.mimeMappings.get("html")).isEqualTo("text/html;charset=utf-8")
        assertThat(container.mimeMappings.get("json")).isEqualTo("text/html;charset=utf-8")
        if (container.documentRoot != null) {
            assertThat(container.documentRoot.path).isEqualTo(FilenameUtils.separatorsToSystem("build/www"))
        }

        val builder = Undertow.builder()
        container.builderCustomizers.forEach { c -> c.customize(builder) }
        val serverOptions = ReflectionTestUtils.getField(builder, "serverOptions") as OptionMap.Builder
        assertThat(serverOptions.map.get(UndertowOptions.ENABLE_HTTP2)).isNull()
    }

    @Test
    fun testUndertowHttp2Enabled() {
        props.http.setVersion(JHipsterProperties.Http.Version.V_2_0)
        val container = UndertowEmbeddedServletContainerFactory()
        webConfigurer.customize(container)
        val builder = Undertow.builder()
        container.builderCustomizers.forEach { c -> c.customize(builder) }
        val serverOptions = ReflectionTestUtils.getField(builder, "serverOptions") as OptionMap.Builder
        assertThat(serverOptions.map.get(UndertowOptions.ENABLE_HTTP2)).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterOnApiPath() {
        props.cors.allowedOrigins = listOf("*")
        props.cors.allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE")
        props.cors.allowedHeaders = listOf("*")
        props.cors.maxAge = 1800L
        props.cors.allowCredentials = true

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
            .addFilters<StandaloneMockMvcBuilder>(webConfigurer.corsFilter())
            .build()

        mockMvc.perform(
            options("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"))
            .andExpect(header().string(HttpHeaders.VARY, "Origin"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1800"))

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
        )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"))
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterOnOtherPath() {
        props.cors.allowedOrigins = listOf("*")
        props.cors.allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE")
        props.cors.allowedHeaders = listOf("*")
        props.cors.maxAge = 1800L
        props.cors.allowCredentials = true

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
            .addFilters<StandaloneMockMvcBuilder>(webConfigurer.corsFilter())
            .build()

        mockMvc.perform(
            get("/test/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
        )
            .andExpect(status().isOk)
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterDeactivated() {
        props.cors.allowedOrigins = null

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
            .addFilters<StandaloneMockMvcBuilder>(webConfigurer.corsFilter())
            .build()

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
        )
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterDeactivated2() {
        props.cors.allowedOrigins = ArrayList()

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
            .addFilters<StandaloneMockMvcBuilder>(webConfigurer.corsFilter())
            .build()

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
        )
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    internal class MockFilterRegistration : FilterRegistration, FilterRegistration.Dynamic {

        override fun addMappingForServletNames(dispatcherTypes: EnumSet<DispatcherType>, isMatchAfter: Boolean, vararg servletNames: String) {

        }

        override fun getServletNameMappings(): Collection<String>? {
            return null
        }

        override fun addMappingForUrlPatterns(dispatcherTypes: EnumSet<DispatcherType>, isMatchAfter: Boolean, vararg urlPatterns: String) {

        }

        override fun getUrlPatternMappings(): Collection<String>? {
            return null
        }

        override fun setAsyncSupported(isAsyncSupported: Boolean) {

        }

        override fun getName(): String? {
            return null
        }

        override fun getClassName(): String? {
            return null
        }

        override fun setInitParameter(name: String, value: String): Boolean {
            return false
        }

        override fun getInitParameter(name: String): String? {
            return null
        }

        override fun setInitParameters(initParameters: Map<String, String>): Set<String>? {
            return null
        }

        override fun getInitParameters(): Map<String, String>? {
            return null
        }
    }

    internal class MockServletRegistration : ServletRegistration, ServletRegistration.Dynamic {

        override fun setLoadOnStartup(loadOnStartup: Int) {

        }

        override fun setServletSecurity(constraint: ServletSecurityElement): Set<String>? {
            return null
        }

        override fun setMultipartConfig(multipartConfig: MultipartConfigElement) {

        }

        override fun setRunAsRole(roleName: String) {

        }

        override fun setAsyncSupported(isAsyncSupported: Boolean) {

        }

        override fun addMapping(vararg urlPatterns: String): Set<String>? {
            return null
        }

        override fun getMappings(): Collection<String>? {
            return null
        }

        override fun getRunAsRole(): String? {
            return null
        }

        override fun getName(): String? {
            return null
        }

        override fun getClassName(): String? {
            return null
        }

        override fun setInitParameter(name: String, value: String): Boolean {
            return false
        }

        override fun getInitParameter(name: String): String? {
            return null
        }

        override fun setInitParameters(initParameters: Map<String, String>): Set<String>? {
            return null
        }

        override fun getInitParameters(): Map<String, String>? {
            return null
        }
    }

    class MockHazelcastInstance : HazelcastInstance {

        override fun getName(): String {
            return "HazelcastInstance"
        }

        override fun <E> getQueue(s: String): IQueue<E>? {
            return null
        }

        override fun <E> getTopic(s: String): ITopic<E>? {
            return null
        }

        override fun <E> getSet(s: String): ISet<E>? {
            return null
        }

        override fun <E> getList(s: String): IList<E>? {
            return null
        }

        override fun <K, V> getMap(s: String): IMap<K, V>? {
            return null
        }

        override fun <K, V> getReplicatedMap(s: String): ReplicatedMap<K, V>? {
            return null
        }

        override fun getJobTracker(s: String): JobTracker? {
            return null
        }

        override fun <K, V> getMultiMap(s: String): MultiMap<K, V>? {
            return null
        }

        override fun getLock(s: String): ILock? {
            return null
        }

        override fun <E> getRingbuffer(s: String): Ringbuffer<E>? {
            return null
        }

        override fun <E> getReliableTopic(s: String): ITopic<E>? {
            return null
        }

        override fun getCluster(): Cluster? {
            return null
        }

        override fun getLocalEndpoint(): Endpoint? {
            return null
        }

        override fun getExecutorService(s: String): IExecutorService? {
            return null
        }

        override fun getDurableExecutorService(s: String): DurableExecutorService? {
            return null
        }

        @Throws(TransactionException::class)
        override fun <T> executeTransaction(transactionalTask: TransactionalTask<T>): T? {
            return null
        }

        @Throws(TransactionException::class)
        override fun <T> executeTransaction(transactionOptions: TransactionOptions, transactionalTask: TransactionalTask<T>): T? {
            return null
        }

        override fun newTransactionContext(): TransactionContext? {
            return null
        }

        override fun newTransactionContext(transactionOptions: TransactionOptions): TransactionContext? {
            return null
        }

        override fun getIdGenerator(s: String): IdGenerator? {
            return null
        }

        override fun getAtomicLong(s: String): IAtomicLong? {
            return null
        }

        override fun <E> getAtomicReference(s: String): IAtomicReference<E>? {
            return null
        }

        override fun getCountDownLatch(s: String): ICountDownLatch? {
            return null
        }

        override fun getSemaphore(s: String): ISemaphore? {
            return null
        }

        override fun getDistributedObjects(): Collection<DistributedObject>? {
            return null
        }

        override fun addDistributedObjectListener(distributedObjectListener: DistributedObjectListener): String? {
            return null
        }

        override fun removeDistributedObjectListener(s: String): Boolean {
            return false
        }

        override fun getConfig(): Config? {
            return null
        }

        override fun getPartitionService(): PartitionService? {
            return null
        }

        override fun getQuorumService(): QuorumService? {
            return null
        }

        override fun getClientService(): ClientService? {
            return null
        }

        override fun getLoggingService(): LoggingService? {
            return null
        }

        override fun getLifecycleService(): LifecycleService? {
            return null
        }

        override fun <T : DistributedObject> getDistributedObject(s: String, s1: String): T? {
            return null
        }

        override fun getUserContext(): ConcurrentMap<String, Any>? {
            return null
        }

        override fun getXAResource(): HazelcastXAResource? {
            return null
        }

        override fun getCacheManager(): ICacheManager? {
            return null
        }

        override fun shutdown() {

        }
    }

}
