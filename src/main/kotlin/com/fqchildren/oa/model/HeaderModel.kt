package com.fqchildren.oa.model

import com.fqchildren.oa.base.BaseModel

/**
 * 请求头数据模型
 */
data class HeaderModel(
        // 客户端IP地址
        var ip: String = "",
        // 当前时间戳
        var time: Long = 0,
        // 客户端设备ID
        var id: String = "",
        // 客户端类型
        var type: String = "",
        // 客户端版本
        var ver: Int = 0,
        // 身份Token信息
        var token: String = ""
) : BaseModel()
