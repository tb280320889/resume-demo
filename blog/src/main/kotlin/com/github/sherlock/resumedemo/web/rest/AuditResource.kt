package com.github.sherlock.resumedemo.web.rest

import com.github.sherlock.resumedemo.service.AuditEventService
import com.github.sherlock.resumedemo.web.rest.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import io.swagger.annotations.ApiParam
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneId

/**
 * Created by TangBin on 2017/9/28.
 *
 * REST controller for getting the audit events.
 */
@RestController
@RequestMapping("/management/audits")
class AuditResource
constructor(
    private val auditEventService: AuditEventService
) {
    /**
     * GET  /audits : get a page of AuditEvents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping
    fun getAll(@ApiParam pageable: Pageable): ResponseEntity<List<AuditEvent>> {
        val page = auditEventService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /audits : get a page of AuditEvents between the fromDate and toDate.
     *
     * @param fromDate the start of the time period of AuditEvents to get
     * @param toDate the end of the time period of AuditEvents to get
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping(params = arrayOf("fromDate", "toDate"))
    fun getByDates(
        @RequestParam(value = "fromDate") fromDate: LocalDate,
        @RequestParam(value = "toDate") toDate: LocalDate,
        @ApiParam pageable: Pageable
    ): ResponseEntity<List<AuditEvent>> {

        val page = auditEventService.findByDates(
            fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant(),
            pageable
        )
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /audits/:id : get an AuditEvent by id.
     *
     * @param id the id of the entity to get
     * @return the ResponseEntity with status 200 (OK) and the AuditEvent in body, or status 404 (Not Found)
     */
    @GetMapping("/{id:.+}")
    operator fun get(@PathVariable id: Long?): ResponseEntity<AuditEvent> {
        return ResponseUtil.wrapOrNotFound(auditEventService.find(id))
    }
}
