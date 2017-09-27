package com.github.sherlock.resumedemo.service.mapper

import com.github.sherlock.resumedemo.domain.Authority
import com.github.sherlock.resumedemo.domain.User
import com.github.sherlock.resumedemo.service.dto.UserDTO
import org.springframework.beans.BeanUtils

/**
 * Created by TangBin on 2017/9/27.
 *
 * Mapper for the entity User and its DTO called UserDTO.
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
class UserMapper {

  fun userToUserDTO(user: User): UserDTO = UserDTO(user)

  fun usersToUserDTOs(users: List<User?>): List<UserDTO> {
    return users.filterNotNull().map { userToUserDTO(it) }
  }

  fun userDTOToUser(userDTO: UserDTO?): User? {
    return if (userDTO == null) {
      null
    } else {
      val user = User()
      BeanUtils.copyProperties(userDTO, user)
      user
    }
  }

  fun userDTOsToUsers(userDTOs: List<UserDTO?>): List<User> {
    return userDTOs.filterNotNull().map { userDTOToUser(it)!! }
  }

  fun userFromId(id: Long?): User? {
    return if (id != null) {
      val user = User()
      user.id = id
      user
    } else null
  }

  fun authoritiesFromStrings(strings: Set<String>): Set<Authority> {
    return strings.mapTo(HashSet()) { Authority(name = it) }
  }

}
