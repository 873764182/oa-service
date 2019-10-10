package com.fqchildren.oa.filter

import com.fqchildren.oa.base.BaseFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 接口名称规范约束，所有的OA业务接口必须以指定的方式命名，方便权限控制
 */
@Component
class NameFilter : BaseFilter() {
    private val log = LoggerFactory.getLogger(NameFilter::class.java)

    /**
     * do: 对于不易归类的用这个后缀，如登陆等不好归类的接口
     * sel: select  查询
     * ins: insert  插入
     * upd: update  更新
     * del: delete  删除
     */
    private val suffix = listOf("do", "sel", "ins", "upd", "del")

    override fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val uri = request.requestURI
        val block = uri.split("/")
        if (block.isEmpty()) {
            log.error("访问的URI非法，无法解析，访问被拦截 -> $uri")
            response(response, 403, "访问的URI非法，无法解析，访问被拦截 -> $uri")
            return
        }
        val method = block[block.size - 1]
        if (!testAndVerify(method)) {
            log.error("URI未按要求定义，拒绝访问 -> $method")
            response(response, 403, "URI未按要求定义，拒绝访问 -> $method")
        } else {
            chain.doFilter(request, response)
        }
    }

    fun testAndVerify(method: String): Boolean {
        if (!method.contains(".")) {
            return false
        }
        var flag = false
        suffix.forEach {
            if (method.endsWith(it)) {
                flag = true
            }
        }
        return flag
    }
}
