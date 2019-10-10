package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 角色信息表
 */
data class RoleInfo(
        var name: String = "",
        var depict: String = ""
) : BaseTable()
