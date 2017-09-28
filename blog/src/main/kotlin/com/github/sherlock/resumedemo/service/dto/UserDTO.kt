package com.github.sherlock.resumedemo.service.dto

import com.github.sherlock.resumedemo.config.constants.ConstantsKT
import com.github.sherlock.resumedemo.domain.User
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.springframework.beans.BeanUtils
import java.time.Instant
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * Created by TangBin on 2017/9/27.
 *
 * A DTO representing a user, with his authorities.
 */

open class UserDTO
(

    open var id: Long? = null,

    @NotBlank
    @Pattern(regexp = ConstantsKT.LOGIN_REGEX)
    @Size(min = 1, max = 100)
    open var login: String? = null,

    @Size(max = 50)
    open var firstName: String? = null,

    @Size(max = 50)
    open var lastName: String? = null,

    @Email
    @Size(min = 5, max = 100)
    open var email: String? = null,

    @Size(max = 256)
    open var imageUrl: String? = null,
    open var activated: Boolean = false,

    @Size(min = 2, max = 5)
    open var langKey: String? = null,
    open var createdBy: String? = null,
    open var createdDate: Instant? = null,
    open var lastModifiedBy: String? = null,
    open var lastModifiedDate: Instant? = null,
    open var authorities: Set<String>? = null

) {
    constructor(user: User) : this() {
        BeanUtils.copyProperties(user, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserDTO) return false

        if (id != other.id) return false
        if (login != other.login) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (email != other.email) return false
        if (imageUrl != other.imageUrl) return false
        if (activated != other.activated) return false
        if (langKey != other.langKey) return false
        if (createdBy != other.createdBy) return false
        if (createdDate != other.createdDate) return false
        if (lastModifiedBy != other.lastModifiedBy) return false
        if (lastModifiedDate != other.lastModifiedDate) return false
        if (authorities != other.authorities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (login?.hashCode() ?: 0)
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + activated.hashCode()
        result = 31 * result + (langKey?.hashCode() ?: 0)
        result = 31 * result + (createdBy?.hashCode() ?: 0)
        result = 31 * result + (createdDate?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedBy?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedDate?.hashCode() ?: 0)
        result = 31 * result + (authorities?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "UserDTO(id=$id, login=$login, firstName=$firstName, lastName=$lastName, email=$email, imageUrl=$imageUrl, activated=$activated, langKey=$langKey, createdBy=$createdBy, createdDate=$createdDate, lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, authorities=$authorities)"
    }


}
