package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 用户角色表
 */
data class UserRole(
        var userId: String = "",
        var roleId: String = ""
) : BaseTable()
