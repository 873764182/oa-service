package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.RestModel
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * 监听全局错误
 *
 * https://blog.csdn.net/htjl575896870/article/details/90715271
 */
@RestController
class ErrorRest(error: ErrorAttributes) : AbstractErrorController(error), BaseRest {
    private val log = LoggerFactory.getLogger(ErrorRest::class.java)

    companion object {
        private const val ERROR_PATH = "/error"
    }

    /**
     * 通知系统新的错误页面地址，有错误请转发到这个路径
     */
    override fun getErrorPath(): String {
        return ERROR_PATH
    }

    /**
     * 接收错误地址访问
     */
    @RequestMapping(value = [ERROR_PATH, "$ERROR_PATH.do"])
    fun error(request: HttpServletRequest, response: HttpServletResponse): RestModel {
        val message = getErrorAttributes(request, false)
        log.error("请求 ${request.requestURI} 发生异常，异常信息：$message}")
        return RestModel(code = Int.MIN_VALUE, msg = "error", data = message)
    }
}
