package com.github.sherlock.resumedemo.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

/**
 * Created by TangBin on 2017/9/27.
 */
@Entity
@Table(name = "jhi_social_user_connection")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class SocialUserConnection
(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @NotNull
    @Column(name = "user_id", length = 255, nullable = false)
    var userId: String? = null,

    @NotNull
    @Column(name = "provider_id", length = 255, nullable = false)
    var providerId: String? = null,

    @NotNull
    @Column(name = "provider_user_id", length = 255, nullable = false)
    var providerUserId: String? = null,

    @NotNull
    @Column(nullable = false)
    var rank: Long? = null,

    @Column(name = "display_name", length = 255)
    var displayName: String? = null,

    @Column(name = "profile_url", length = 255)
    var profileURL: String? = null,

    @Column(name = "image_url", length = 255)
    var imageURL: String? = null,

    @NotNull
    @Column(name = "access_token", length = 255, nullable = false)
    var accessToken: String? = null,

    @Column(length = 255)
    var secret: String? = null,

    @Column(name = "refresh_token", length = 255)
    var refreshToken: String? = null,

    @Column(name = "expire_time")
    var expireTime: Long? = null
) : Serializable {

  companion object {
    private val serialVersionUID = -5848912306L
  }
}
