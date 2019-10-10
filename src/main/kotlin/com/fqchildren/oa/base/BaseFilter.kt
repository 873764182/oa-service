package com.fqchildren.oa.base

import org.json.JSONObject
import org.slf4j.LoggerFactory
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 基础过滤器，做一些基本过滤处理，所有的过滤器仅针对POST方法有效
 */
abstract class BaseFilter : Filter {
    private val log = LoggerFactory.getLogger(BaseFilter::class.java)

    override fun init(filterConfig: FilterConfig) {
        log.info("过滤器 ${this.javaClass.name} 初始化")
    }

    override fun destroy() {
        log.info("过滤器 ${this.javaClass.name} 销毁")
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (request == null || response == null || chain == null) {
            log.info("请求体异常，不过滤")
        } else if (request !is HttpServletRequest || response !is HttpServletResponse) {
            chain.doFilter(request, response)
            log.info("非处理（包装）的请求，不过滤")
        } else if (request.method.toUpperCase() != "POST") {
            chain.doFilter(request, response)
            log.info("非POST的请求，不过滤")
        } else {
            filter(request, response, chain)
        }
    }

    protected fun response(response: ServletResponse, code: Int = 100, msg: String = "error") {
        val data = JSONObject()
        data.put("code", code)
        data.put("msg", msg)
        if (response is HttpServletResponse) {
            response.addHeader("Access-Control-Allow-Origin", "*")
        }
        response.contentType = "application/json; charset=utf-8"
        response.writer.write(data.toString());
        response.writer.flush();
        // response.writer.close();
        response.flushBuffer();
    }

    protected abstract fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain)
}
