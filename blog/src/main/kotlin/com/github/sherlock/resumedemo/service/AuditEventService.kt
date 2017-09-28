package com.github.sherlock.resumedemo.service

import com.github.sherlock.resumedemo.config.audit.AuditEventConverter
import com.github.sherlock.resumedemo.repository.PersistenceAuditEventRepository
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Created by TangBin on 2017/9/27.
 *
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator AuditEventRepository
 */
@Service
@Transactional
class AuditEventService constructor(
    private val persistenceAuditEventRepository: PersistenceAuditEventRepository, private val auditEventConverter: AuditEventConverter
) {


    fun findAll(pageable: Pageable): Page<AuditEvent> {
        return persistenceAuditEventRepository.findAll(pageable)
            .map { auditEventConverter.convertToAuditEvent(it) }
    }

    fun findByDates(fromDate: Instant, toDate: Instant, pageable: Pageable): Page<AuditEvent> {
        return persistenceAuditEventRepository
            .findAllByAuditEventDateBetween(fromDate, toDate, pageable)
            .map { auditEventConverter.convertToAuditEvent(it) }
    }

    fun find(id: Long?): Optional<AuditEvent> {
        return Optional.ofNullable(persistenceAuditEventRepository.findOne(id))
            .map { auditEventConverter.convertToAuditEvent(it) }
    }

}
