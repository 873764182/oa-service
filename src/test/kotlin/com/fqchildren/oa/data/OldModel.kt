package com.fqchildren.oa.data

import com.google.gson.Gson

data class OldModel(
        var Id: Int = 0,
        var Name: String = "",
        var ParentId: Int = 0,
        var ShortName: String = "",
        var LevelType: Int = 0,
        var CityCode: String = "",
        var ZipCode: String = "",
        var MergerName: String = "",
        var Lng: Float = 0f,
        var Lat: Float = 0f,
        var Pinyin: String = ""
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
