package com.github.sherlock.resumedemo.service.dto

import com.github.sherlock.resumedemo.config.ConstantsKT
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

data class UserDTO
(

    var id: Long? = null,

    @NotBlank
    @Pattern(regexp = ConstantsKT.LOGIN_REGEX)
    @Size(min = 1, max = 100)
    var login: String? = null,

    @Size(max = 50)
    var firstName: String? = null,

    @Size(max = 50)
    var lastName: String? = null,

    @Email
    @Size(min = 5, max = 100)
    var email: String? = null,

    @Size(max = 256)
    var imageUrl: String? = null,

    var activated: Boolean = false,

    @Size(min = 2, max = 5)
    var langKey: String? = null,

    var createdBy: String? = null,

    var createdDate: Instant? = null,

    var lastModifiedBy: String? = null,

    var lastModifiedDate: Instant? = null,

    var authorities: Set<String>? = null

)
{
  constructor(user: User) : this()
  {
    BeanUtils.copyProperties(user, this)
  }
}
