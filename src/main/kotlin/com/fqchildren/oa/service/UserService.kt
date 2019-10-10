package com.fqchildren.oa.service

import com.fqchildren.oa.table.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService {

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    /**
     * 删除一个用户，同时相关的用户角色等数据也会被删除,返回受影响的数据统计
     */
    @Transactional
    fun deleteUser(userId: String): Map<String, Any> {
        val role = userRoleMapper.delete(UserRole(userId = userId).sql())
        val user = userInfoMapper.delete(UserInfo().sql(uid = userId))
        return mapOf("user" to user, "role" to role)
    }

    /**
     * 更新绑定角色
     */
    @Transactional
    fun bindingRoles(userId: String, roleIds: String): Map<String, Int> {
        val deleteCount = userRoleMapper.delete(UserRole(userId = userId).sql())
        var insertCount = 0
        if (roleIds.isEmpty()) {
            return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
        }
        if (!roleIds.contains("&")) {
            val userRole = UserRole()
            userRole.userId = userId
            userRole.roleId = roleIds
            insertCount = userRoleMapper.insert(userRole)
            return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
        }
        roleIds.split("&").forEach {
            val userRole = UserRole()
            userRole.userId = userId
            userRole.roleId = it
            insertCount += userRoleMapper.insert(userRole)
        }
        return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
    }

}
