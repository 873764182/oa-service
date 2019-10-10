package com.fqchildren.oa.model

import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

class ResponseModel constructor(response: HttpServletResponse) : HttpServletResponseWrapper(response) {
    private val log = LoggerFactory.getLogger(ResponseModel::class.java)

    init {
        log.info("响应对象 ${this.javaClass.name} 初始化")
    }

}
