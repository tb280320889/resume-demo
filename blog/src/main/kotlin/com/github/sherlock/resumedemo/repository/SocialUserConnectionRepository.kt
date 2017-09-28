package com.github.sherlock.resumedemo.repository

import com.github.sherlock.resumedemo.domain.SocialUserConnection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionRepository

/**
 * Created by TangBin on 2017/9/27.
 *
 * Spring Data JPA repository for the Social User Connection entity.
 */

interface SocialUserConnectionRepository : JpaRepository<SocialUserConnection, Long> {
    /**
     *
     * @param providerId
     * @param providerUserId
     * @return
     */
    fun findAllByProviderIdAndProviderUserId(providerId: String, providerUserId: String): List<SocialUserConnection>

    /**
     *
     * @param providerId
     * @param providerUserIds
     * @return
     */
    fun findAllByProviderIdAndProviderUserIdIn(providerId: String, providerUserIds: Set<String>): List<SocialUserConnection>

    /**
     *
     * @param userId
     * @return
     */
    fun findAllByUserIdOrderByProviderIdAscRankAsc(userId: String): List<SocialUserConnection>

    /**
     *
     * @param userId
     * @param providerId
     * @return
     */
    fun findAllByUserIdAndProviderIdOrderByRankAsc(userId: String, providerId: String): List<SocialUserConnection>

    /**
     *
     * @param userId
     * @param providerId
     * @param provideUserId
     * @return
     */
    fun findAllByUserIdAndProviderIdAndProviderUserIdIn(userId: String, providerId: String, provideUserId: List<String>): List<SocialUserConnection>

    /**
     *
     * @param userId
     * @param providerId
     * @param providerUserId
     * @return
     */
    fun findOneByUserIdAndProviderIdAndProviderUserId(userId: String, providerId: String, providerUserId: String): SocialUserConnection?

    /**
     *
     * @param userId
     * @param providerId
     */
    fun deleteByUserIdAndProviderId(userId: String, providerId: String)

    /**
     *
     * @param userId
     * @param providerId
     * @param providerUserId
     */
    fun deleteByUserIdAndProviderIdAndProviderUserId(userId: String, providerId: String, providerUserId: String)

    fun findUserIdsConnectedTo(providerId: String, providerUserIds: Set<String>): Set<String?>
    fun createConnectionRepository(userId: String?): ConnectionRepository
    fun findUserIdsWithConnection(connection: Connection<*>): List<String?>
}
