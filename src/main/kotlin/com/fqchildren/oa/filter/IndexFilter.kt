package com.fqchildren.oa.filter

import com.fqchildren.oa.base.BaseFilter
import com.fqchildren.oa.model.RequestModel
import com.fqchildren.oa.model.ResponseModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 全局过滤器，对 request、response 进行必要的包装
 */
@Component
class IndexFilter : BaseFilter() {
    private val log = LoggerFactory.getLogger(IndexFilter::class.java)

    override fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        chain.doFilter(RequestModel(request), ResponseModel(response))
    }
}
