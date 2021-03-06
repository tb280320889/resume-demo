package com.github.sherlock.resumedemo.web.rest

import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.config.audit.AuditEventConverter
import com.github.sherlock.resumedemo.domain.PersistentAuditEvent
import com.github.sherlock.resumedemo.repository.PersistenceAuditEventRepository
import com.github.sherlock.resumedemo.service.AuditEventService
import org.hamcrest.Matchers.hasItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Created by TangBin on 2017/9/28.
 */


@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
@Transactional
class AuditResourceIntTest {

    @Autowired
    private lateinit var auditEventRepository: PersistenceAuditEventRepository
    @Autowired
    private lateinit var auditEventConverter: AuditEventConverter
    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter
    @Autowired
    private lateinit var formattingConversionService: FormattingConversionService
    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver
    private lateinit var auditEvent: PersistentAuditEvent
    private lateinit var restAuditMockMvc: MockMvc

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val auditEventService = AuditEventService(auditEventRepository, auditEventConverter)
        val auditResource = AuditResource(auditEventService)
        this.restAuditMockMvc = MockMvcBuilders.standaloneSetup(auditResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setConversionService(formattingConversionService)
            .setMessageConverters(jacksonMessageConverter).build()
    }

    @Before
    fun initTest() {
        auditEventRepository.deleteAll()
        auditEvent = PersistentAuditEvent()
        auditEvent.auditEventType = (SAMPLE_TYPE)
        auditEvent.principal = (SAMPLE_PRINCIPAL)
        auditEvent.auditEventDate = (SAMPLE_TIMESTAMP)
    }

    @Test
    @Throws(Exception::class)
    fun getAllAudits() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Get all the audits
        restAuditMockMvc.perform(get("/management/audits"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].principal").value(hasItem<String>(SAMPLE_PRINCIPAL)))
    }

    @Test
    @Throws(Exception::class)
    fun getAudit() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Get the audit
        restAuditMockMvc.perform(get("/management/audits/{id}", auditEvent.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.principal").value(SAMPLE_PRINCIPAL))
    }

    @Test
    @Throws(Exception::class)
    fun getAuditsByDate() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Generate dates for selecting audits by date, making sure the period will contain the audit
        val fromDate = SAMPLE_TIMESTAMP.minusSeconds(SECONDS_PER_DAY).toString().substring(0, 10)
        val toDate = SAMPLE_TIMESTAMP.plusSeconds(SECONDS_PER_DAY).toString().substring(0, 10)

        // Get the audit
        restAuditMockMvc.perform(get("/management/audits?fromDate=$fromDate&toDate=$toDate"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].principal").value(hasItem<String>(SAMPLE_PRINCIPAL)))
    }

    @Test
    @Throws(Exception::class)
    fun getNonExistingAuditsByDate() {
        // Initialize the database
        auditEventRepository.save(auditEvent)

        // Generate dates for selecting audits by date, making sure the period will not contain the sample audit
        val fromDate = SAMPLE_TIMESTAMP.minusSeconds(2 * SECONDS_PER_DAY).toString().substring(0, 10)
        val toDate = SAMPLE_TIMESTAMP.minusSeconds(SECONDS_PER_DAY).toString().substring(0, 10)

        // Query audits but expect no results
        restAuditMockMvc.perform(get("/management/audits?fromDate=$fromDate&toDate=$toDate"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header().string("X-Total-Count", "0"))
    }

    @Test
    @Throws(Exception::class)
    fun getNonExistingAudit() {
        // Get the audit
        restAuditMockMvc.perform(get("/management/audits/{id}", java.lang.Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    companion object {

        private val SAMPLE_PRINCIPAL = "SAMPLE_PRINCIPAL"
        private val SAMPLE_TYPE = "SAMPLE_TYPE"
        private val SAMPLE_TIMESTAMP = Instant.parse("2015-08-04T10:11:30Z")
        private val SECONDS_PER_DAY = (60 * 60 * 24).toLong()
    }
}
