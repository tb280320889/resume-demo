package com.github.sherlock.resumedemo.web.rest.vm

import com.github.sherlock.resumedemo.service.dto.UserDTO
import java.time.Instant
import javax.validation.constraints.Size

/**
 * Created by TangBin on 2017/9/28.
 *
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 */
data class ManagedUserVM
(
    override var id: Long? = null,
    override var login: String? = null,

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    var password: String? = null,

    override var firstName: String? = null,
    override var lastName: String? = null,
    override var email: String? = null,
    override var activated: Boolean = false,
    override var imageUrl: String? = null,
    override var langKey: String? = null,
    override var createdBy: String? = null,
    override var createdDate: Instant? = null,
    override var lastModifiedBy: String? = null,
    override var lastModifiedDate: Instant? = null,
    override var authorities: Set<String>? = null
) : UserDTO() {

    constructor() : this(null) {
        // Empty constructor needed for Jackson.
    }

    override fun toString(): String {
        return "ManagedUserVM{" +
               "} " + super.toString()
    }

    companion object {
        const val PASSWORD_MIN_LENGTH = 4
        const val PASSWORD_MAX_LENGTH = 100
    }
}
