package com.github.sherlock.resumedemo.config.audit

import com.github.sherlock.resumedemo.domain.PersistentAuditEvent
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by TangBin on 2017/9/27.
 */

@Component
class AuditEventConverter {

  /**
   * Convert a list of PersistentAuditEvent to a list of AuditEvent
   *
   * @param persistentAuditEvents the list to convert
   * @return the converted list.
   */
  fun convertToAuditEvent(persistentAuditEvents: Iterable<PersistentAuditEvent>?): List<AuditEvent?> {
    if (persistentAuditEvents == null) {
      return emptyList()
    }
    return persistentAuditEvents.map { convertToAuditEvent(it) }
  }

  /**
   * Convert a PersistentAuditEvent to an AuditEvent
   *
   * @param persistentAuditEvent the event to convert
   * @return the converted list.
   */
  fun convertToAuditEvent(persistentAuditEvent: PersistentAuditEvent?): AuditEvent? {
    return persistentAuditEvent?.let {
      AuditEvent(
          Date.from(persistentAuditEvent.auditEventDate),
          persistentAuditEvent.principal,
          persistentAuditEvent.auditEventType,
          convertDataToObjects(persistentAuditEvent.data)
      )
    }
  }

  /**
   * Internal conversion. This is needed to support the current SpringBoot actuator AuditEventRepository interface
   *
   * @param data the data to convert
   * @return a map of String, Object
   */
  fun convertDataToObjects(data: Map<String, String>?): Map<String, Any> = data ?: HashMap()

  /**
   * Internal conversion. This method will allow to save additional data.
   * By default, it will save the object as string
   *
   * @param data the data to convert
   * @return a map of String, String
   */
  fun convertDataToStrings(data: Map<String, Any?>?): Map<String, String> {
    val results = HashMap<String, String>()

    if (data != null) {
      for ((key, obj) in data) {

        // Extract the data that will be saved.
        when {
          obj is WebAuthenticationDetails -> {
            results.put("remoteAddress", obj.remoteAddress)
            results.put("sessionId", obj.sessionId)
          }
          obj != null -> results.put(key, obj.toString())
          else -> results.put(key, "null")
        }
      }
    }

    return results
  }
}
