package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 用户信息表
 */
data class UserInfo(
        var phone: String = "",
        var email: String = "",
        var wxOid: String = "",
        var wxAid: String = "",
        var username: String = "",
        var password: String = "",
        var photo: String = "",
        var gender: Int = 0,
        var regionId: String = "",
        var depId: String = ""
) : BaseTable()
