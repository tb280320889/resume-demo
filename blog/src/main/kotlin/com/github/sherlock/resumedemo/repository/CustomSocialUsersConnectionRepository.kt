package com.github.sherlock.resumedemo.repository

import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.UsersConnectionRepository

/**
 * Created by TangBin on 2017/9/27.
 */

class CustomSocialUsersConnectionRepository
(
    private val socialUserConnectionRepository: SocialUserConnectionRepository,
    private val connectionFactoryLocator: ConnectionFactoryLocator
) : UsersConnectionRepository {

    override fun findUserIdsWithConnection(connection: Connection<*>): List<String?> {
        val key = connection.key
        val socialUserConnections = socialUserConnectionRepository.findAllByProviderIdAndProviderUserId(key.providerId, key.providerUserId)
        return socialUserConnections.map { it.userId }
    }

    override fun findUserIdsConnectedTo(providerId: String, providerUserIds: Set<String>): Set<String?> {
        val socialUserConnections = socialUserConnectionRepository.findAllByProviderIdAndProviderUserIdIn(providerId, providerUserIds)
        return socialUserConnections.mapTo(HashSet()) { it.userId }
    }

    override fun createConnectionRepository(userId: String?): ConnectionRepository {
        if (userId == null) {
            throw IllegalArgumentException("userId cannot be null")
        }
        return CustomSocialConnectionRepository(userId, socialUserConnectionRepository, connectionFactoryLocator)
    }
}
