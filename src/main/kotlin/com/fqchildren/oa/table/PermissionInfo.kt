package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 权限信息表
 */
data class PermissionInfo(
        var name: String = "",
        var depict: String = ""
) : BaseTable()
