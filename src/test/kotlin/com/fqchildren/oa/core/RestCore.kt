package com.fqchildren.oa.core

import com.fqchildren.oa.utils.HttpUtils

object RestCore {
//    private const val base_url = "http://localhost:8085"
    private const val base_url = "http://192.168.50.100:8085"

    fun json(url: String, params: Map<String, Any> = getDefParams(), header: Map<String, String> = getDefHeader()): String? {
        var requestUrl = url
        if (!requestUrl.startsWith("http")) {
            requestUrl = base_url + requestUrl
        }
        return HttpUtils.JSON(requestUrl, params, header)
    }

    private fun getDefParams(): Map<String, Any> {
        return mapOf()
    }

    private fun getDefHeader(): Map<String, String> {
        return mapOf(
                "h-time" to System.currentTimeMillis().toString(),
                "h-id" to "dev",
                "h-type" to "idea",
                "h-ver" to (20190827).toString(),
                "h-token" to "61FEFC08BB8DA85546C6EE56F925F35479020735E4672C4EF1BFBFBB5FFAD420"
        )
    }
}
