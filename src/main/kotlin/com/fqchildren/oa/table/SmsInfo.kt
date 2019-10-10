package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 短信信息表
 */
data class SmsInfo(
        var phone: String = "",
        var value: String = "",
        var cIp: String = "",
        var cId: String = ""
) : BaseTable()
