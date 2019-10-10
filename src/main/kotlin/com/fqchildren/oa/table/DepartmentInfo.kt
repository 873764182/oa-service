package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 部门信息表
 */
data class DepartmentInfo(
        var pid: String = "",
        var name: String = "",
        var code: String = "",
        var adminUser: String = "",
        var regionId: String = ""
) : BaseTable()
