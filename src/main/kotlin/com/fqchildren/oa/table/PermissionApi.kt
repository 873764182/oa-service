package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 权限接口表
 */
data class PermissionApi(
        var perId: String = "",
        var name: String = "",
        var depict: String = "",
        var api: String = ""
) : BaseTable()
