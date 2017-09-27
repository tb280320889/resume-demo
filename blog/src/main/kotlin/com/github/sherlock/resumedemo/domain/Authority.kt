package com.github.sherlock.resumedemo.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Created by TangBin on 2017/9/27.
 * An authority (a security role) used by Spring Security.
 */

@Entity
@Table(name = "jhi_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class Authority
(

    @NotNull
    @Size(min = 0, max = 50)
    @Id
    @Column(length = 50)
    var name: String? = null

) : Serializable {
  companion object {
    private val serialVersionUID = -584892312L
  }
}
