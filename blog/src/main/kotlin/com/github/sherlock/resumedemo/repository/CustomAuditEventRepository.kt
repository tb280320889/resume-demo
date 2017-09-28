package com.github.sherlock.resumedemo.repository

import com.github.sherlock.resumedemo.config.audit.AuditEventConverter
import com.github.sherlock.resumedemo.config.constants.ConstantsKT
import com.github.sherlock.resumedemo.domain.PersistentAuditEvent
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Created by TangBin on 2017/9/27.
 */

class CustomAuditEventRepository
(
    private val persistenceAuditEventRepository: PersistenceAuditEventRepository,
    private val auditEventConverter: AuditEventConverter
) : AuditEventRepository {
    private val AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE"
    override fun find(after: Date): List<AuditEvent?> {
        val persistentAuditEvents = persistenceAuditEventRepository.findByAuditEventDateAfter(after.toInstant())
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents)
    }

    override fun find(principal: String?, after: Date?): List<AuditEvent?> {
        val persistentAuditEvents =
            if (principal == null && after == null) {
                persistenceAuditEventRepository.findAll()
            } else if (after == null) {
                persistenceAuditEventRepository.findByPrincipal(principal!!)
            } else {
                persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfter(principal!!, after.toInstant())
            }
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents)
    }

    override fun find(principal: String, after: Date, type: String): List<AuditEvent?> {
        val persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after.toInstant(), type)
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun add(event: AuditEvent) {
        if (AUTHORIZATION_FAILURE != event.type && ConstantsKT.ANONYMOUS_USER != event.principal) {

            with(PersistentAuditEvent()) {
                principal = event.principal
                auditEventType = event.type
                auditEventDate = event.timestamp.toInstant()
                data = auditEventConverter.convertDataToStrings(event.data)
                persistenceAuditEventRepository.save(this)
            }
        }
    }
}
