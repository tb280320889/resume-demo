package com.github.sherlock.resumedemo.domain

import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.MapKeyColumn
import javax.persistence.Table
import javax.validation.constraints.NotNull

/**
 * Created by TangBin on 2017/9/27.
 *
 * Persist AuditEvent managed by the Spring Boot actuator
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Entity
@Table(name = "jhi_persistent_audit_event")
data class PersistentAuditEvent
(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    var id: Long? = null,

    @NotNull
    @Column(nullable = false)
    var principal: String? = null,

    @Column(name = "event_date")
    var auditEventDate: Instant? = null,
    @Column(name = "event_type")
    var auditEventType: String? = null,

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "jhi_persistent_audit_evt_data", joinColumns = arrayOf(JoinColumn(name = "event_id")))
    var data: Map<String, String> = HashMap()

) : Serializable {
    companion object {
        private val serialVersionUID = -584122018L
    }
}
