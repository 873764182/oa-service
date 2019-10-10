package com.fqchildren.oa.base

import org.json.JSONObject
import javax.servlet.http.HttpServletResponse

interface BaseRest {

    fun default() {

    }

    fun response(response: HttpServletResponse, code: Int = 0, msg: String = "") {
        val data = JSONObject()
        data.put("code", code)
        data.put("msg", msg)
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.contentType = "application/json; charset=utf-8"
        response.writer.write(data.toString());
        response.writer.flush();
        response.flushBuffer();
    }

}
