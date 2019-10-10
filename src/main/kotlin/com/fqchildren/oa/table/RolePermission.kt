package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 角色权限表
 */
data class RolePermission(
        var roleId: String = "",
        var perId: String = ""
) : BaseTable()
