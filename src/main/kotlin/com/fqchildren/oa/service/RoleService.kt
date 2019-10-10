package com.fqchildren.oa.service

import com.fqchildren.oa.table.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService {

    @Autowired
    private lateinit var roleInfoMapper: RoleInfoMapper

    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    @Autowired
    private lateinit var rolePermissionMapper: RolePermissionMapper

    /**
     * 删除角色，同时删除子角色，更新相关的用户信息，返回受影响的数据统计
     */
    @Transactional
    fun deleteRole(roleId: String): Map<String, Any> {
        val updateUser = userRoleMapper.delete(UserRole(roleId = roleId).sql())    // 删除所
        val deleteRole = roleInfoMapper.delete(RoleInfo().sql(uid = roleId))   // 删除该角色
        return mapOf("updateUser" to updateUser, "deleteRole" to deleteRole)
    }

    /**
     * 为角色绑定权限
     * 1. 删除旧的绑定
     * 2. 添加新的绑定
     */
    @Transactional
    fun bindingPermission(roleId: String, perIds: String): Map<String, Int> {
        val deleteCount = rolePermissionMapper.delete(RolePermission(roleId = roleId).sql())
        var insertCount = 0
        if (perIds.isEmpty()) {
            return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
        }
        if (!perIds.contains("&")) {
            val rolePermission = RolePermission()
            rolePermission.roleId = roleId
            rolePermission.perId = perIds
            insertCount = rolePermissionMapper.insert(rolePermission)
            return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
        }
        perIds.split("&").forEach {
            val rolePermission = RolePermission()
            rolePermission.roleId = roleId
            rolePermission.perId = it
            insertCount += rolePermissionMapper.insert(rolePermission)
        }
        return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
    }

}
