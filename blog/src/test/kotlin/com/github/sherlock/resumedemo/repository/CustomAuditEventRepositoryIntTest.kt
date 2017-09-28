package com.github.sherlock.resumedemo.repository

import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.config.audit.AuditEventConverter
import com.github.sherlock.resumedemo.config.constants.ConstantsKT
import com.github.sherlock.resumedemo.domain.PersistentAuditEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Created by TangBin on 2017/9/27.
 */

/**
 * Test class for the CustomAuditEventRepository customAuditEventRepository class.
 *
 * @see CustomAuditEventRepository
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
@Transactional
class CustomAuditEventRepositoryIntTest {

    @Autowired
    private lateinit var persistenceAuditEventRepository: PersistenceAuditEventRepository

    @Autowired
    private lateinit var auditEventConverter: AuditEventConverter

    private lateinit var customAuditEventRepository: CustomAuditEventRepository

    private lateinit var testUserEvent: PersistentAuditEvent

    private lateinit var testOtherUserEvent: PersistentAuditEvent

    private lateinit var testOldUserEvent: PersistentAuditEvent

    @Before
    fun setup() {
        customAuditEventRepository = CustomAuditEventRepository(persistenceAuditEventRepository, auditEventConverter)
        persistenceAuditEventRepository.deleteAll()
        val oneHourAgo = Instant.now().minusSeconds(3600)

        testUserEvent = PersistentAuditEvent()
        testUserEvent.principal = ("test-user")
        testUserEvent.auditEventType = ("test-type")
        testUserEvent.auditEventDate = (oneHourAgo)
        val data = HashMap<String, String>()
        data.put("test-key", "test-value")
        testUserEvent.data = (data)

        testOldUserEvent = PersistentAuditEvent()
        testOldUserEvent.principal = ("test-user")
        testOldUserEvent.auditEventType = ("test-type")
        testOldUserEvent.auditEventDate = (oneHourAgo.minusSeconds(10000))

        testOtherUserEvent = PersistentAuditEvent()
        testOtherUserEvent.principal = ("other-test-user")
        testOtherUserEvent.auditEventType = ("test-type")
        testOtherUserEvent.auditEventDate = (oneHourAgo)
    }

    @Test
    fun testFindAfter() {
        persistenceAuditEventRepository.save(testUserEvent)
        persistenceAuditEventRepository.save(testOldUserEvent)

        val events = customAuditEventRepository.find(Date.from(testUserEvent.auditEventDate!!.minusSeconds(3600)))
        assertThat<AuditEvent>(events).hasSize(1)
        val event = events[0]!!
        assertThat(event.principal).isEqualTo(testUserEvent.principal)
        assertThat(event.type).isEqualTo(testUserEvent.auditEventType)
        assertThat<String, Any>(event.data).containsKey("test-key")
        assertThat(event.data["test-key"].toString()).isEqualTo("test-value")
        assertThat(event.timestamp).isEqualTo(Date.from(testUserEvent.auditEventDate))
    }

    @Test
    fun testFindByPrincipal() {
        persistenceAuditEventRepository.save(testUserEvent)
        persistenceAuditEventRepository.save(testOldUserEvent)
        persistenceAuditEventRepository.save(testOtherUserEvent)

        val events = customAuditEventRepository
            .find("test-user", Date.from(testUserEvent.auditEventDate!!.minusSeconds(3600)))
        assertThat<AuditEvent>(events).hasSize(1)
        val event = events[0]!!
        assertThat(event.principal).isEqualTo(testUserEvent.principal)
        assertThat(event.type).isEqualTo(testUserEvent.auditEventType)
        assertThat<String, Any>(event.data).containsKey("test-key")
        assertThat(event.data["test-key"].toString()).isEqualTo("test-value")
        assertThat(event.timestamp).isEqualTo(Date.from(testUserEvent.auditEventDate))
    }

    @Test
    fun testFindByPrincipalNotNullAndAfterIsNull() {
        persistenceAuditEventRepository.save(testUserEvent)
        persistenceAuditEventRepository.save(testOtherUserEvent)

        val events = customAuditEventRepository.find("test-user", null)
        assertThat<AuditEvent>(events).hasSize(1)
        assertThat(events[0]!!.principal).isEqualTo("test-user")
    }

    @Test
    fun testFindByPrincipalIsNullAndAfterIsNull() {
        persistenceAuditEventRepository.save(testUserEvent)
        persistenceAuditEventRepository.save(testOtherUserEvent)

        val events = customAuditEventRepository.find(null, null)
        assertThat<AuditEvent>(events).hasSize(2)
        assertThat<AuditEvent>(events).extracting("principal")
            .containsExactlyInAnyOrder("test-user", "other-test-user")
    }

    @Test
    fun findByPrincipalAndType() {
        persistenceAuditEventRepository.save(testUserEvent)
        persistenceAuditEventRepository.save(testOldUserEvent)

        testOtherUserEvent.auditEventType = (testUserEvent.auditEventType)
        persistenceAuditEventRepository.save(testOtherUserEvent)

        val testUserOtherTypeEvent = PersistentAuditEvent()
        testUserOtherTypeEvent.principal = (testUserEvent.principal)
        testUserOtherTypeEvent.auditEventType = ("test-other-type")
        testUserOtherTypeEvent.auditEventDate = (testUserEvent.auditEventDate)
        persistenceAuditEventRepository.save(testUserOtherTypeEvent)

        val events = customAuditEventRepository.find(
            "test-user",
            Date.from(testUserEvent.auditEventDate!!.minusSeconds(3600)), "test-type"
        )
        assertThat<AuditEvent>(events).hasSize(1)
        val event = events[0]!!
        assertThat(event.principal).isEqualTo(testUserEvent.principal)
        assertThat(event.type).isEqualTo(testUserEvent.auditEventType)
        assertThat<String, Any>(event.data).containsKey("test-key")
        assertThat(event.data["test-key"].toString()).isEqualTo("test-value")
        assertThat(event.timestamp).isEqualTo(Date.from(testUserEvent.auditEventDate))
    }

    @Test
    fun addAuditEvent() {
        val data = HashMap<String, Any>()
        data.put("test-key", "test-value")
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository.add(event)
        val persistentAuditEvents = persistenceAuditEventRepository.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.principal).isEqualTo(event.principal)
        assertThat(persistentAuditEvent.auditEventType).isEqualTo(event.type)
        assertThat(persistentAuditEvent.data).containsKey("test-key")
        assertThat(persistentAuditEvent.data["test-key"]).isEqualTo("test-value")
        assertThat(persistentAuditEvent.auditEventDate).isEqualTo(event.timestamp.toInstant())
    }

    @Test
    fun testAddEventWithWebAuthenticationDetails() {
        val session = MockHttpSession(null, "test-session-id")
        val request = MockHttpServletRequest()
        request.session = session
        request.remoteAddr = "1.2.3.4"
        val details = WebAuthenticationDetails(request)
        val data = HashMap<String, Any>()
        data.put("test-key", details)
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository.add(event)
        val persistentAuditEvents = persistenceAuditEventRepository.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.data.get("remoteAddress")).isEqualTo("1.2.3.4")
        assertThat(persistentAuditEvent.data.get("sessionId")).isEqualTo("test-session-id")
    }

    @Test
    fun testAddEventWithNullData() {
        val data = HashMap<String, Any?>()
        data.put("test-key", null)
        val event = AuditEvent("test-user", "test-type", data)
        customAuditEventRepository.add(event)
        val persistentAuditEvents = persistenceAuditEventRepository.findAll()
        assertThat(persistentAuditEvents).hasSize(1)
        val persistentAuditEvent = persistentAuditEvents[0]
        assertThat(persistentAuditEvent.data.get("test-key")).isEqualTo("null")
    }

    @Test
    fun addAuditEventWithAnonymousUser() {
        val data = HashMap<String, Any>()
        data.put("test-key", "test-value")
        val event = AuditEvent(ConstantsKT.ANONYMOUS_USER, "test-type", data)
        customAuditEventRepository.add(event)
        val persistentAuditEvents = persistenceAuditEventRepository.findAll()
        assertThat(persistentAuditEvents).hasSize(0)
    }

    @Test
    fun addAuditEventWithAuthorizationFailureType() {
        val data = HashMap<String, Any>()
        data.put("test-key", "test-value")
        val event = AuditEvent("test-user", "AUTHORIZATION_FAILURE", data)
        customAuditEventRepository.add(event)
        val persistentAuditEvents = persistenceAuditEventRepository.findAll()
        assertThat(persistentAuditEvents).hasSize(0)
    }

}
