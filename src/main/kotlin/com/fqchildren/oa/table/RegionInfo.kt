package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseTable

/**
 * 地区信息表
 */
data class RegionInfo(
        var pid: String = "",
        var name: String = "",
        var shortName: String = "",
        var fullName: String = "",
        var englishName: String = "",
        var levelType: Int = 0,
        var cityCode: String = "",
        var zipCode: String = "",
        var longitude: Float = 0f,
        var latitude: Float = 0f
) : BaseTable()
