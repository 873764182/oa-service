package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 部门助理表
 */
data class DepartmentAssistant(
        var depId: String = "",
        var userId: String = "",
        var manUser: Int = 0,
        var manDep: Int = 0
) : BaseTable()
