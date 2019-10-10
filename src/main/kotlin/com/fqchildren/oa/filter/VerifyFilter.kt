package com.fqchildren.oa.filter

import com.fqchildren.oa.base.BaseFilter
import com.fqchildren.oa.model.RequestModel
import com.fqchildren.oa.rest.AppRest
import com.fqchildren.oa.table.PermissionApiMapper
import com.fqchildren.oa.utils.permission.PerUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.support.WebApplicationContextUtils
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 验证访问，主要验证需要权限控制的接口的用户角色访问权限
 */
@Component
class VerifyFilter : BaseFilter() {
    private val log = LoggerFactory.getLogger(VerifyFilter::class.java)

    private val verifyApisSet = mutableSetOf<String>()

    private lateinit var permissionApiMapper: PermissionApiMapper

    override fun init(filterConfig: FilterConfig) {
        val wac = WebApplicationContextUtils.getWebApplicationContext(filterConfig.servletContext);
        if (wac != null) {
            permissionApiMapper = wac.getBean(PermissionApiMapper::class.java);
        }
    }

    override fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val uri = request.requestURI
        if (!isVerify(uri)) {
            chain.doFilter(request, response)
        } else {
            if (request !is RequestModel) {
                response(response, code = 443, msg = "request not is RequestModel")
            } else {
                when {
                    request.token.uid.isEmpty() -> response(response, code = 443, msg = "please log in first")
                    doVerify(uri, request.token.uid) -> chain.doFilter(request, response)
                    else -> response(response, code = 443, msg = "insufficient permissions")
                }
            }
        }
    }

    /**
     * 是否需要权限验证
     */
    fun isVerify(api: String): Boolean {
        if (verifyApisSet.isEmpty()) {
            val restList = PerUtils.getApiData(AppRest.apis_pack_name)
            restList.forEach { rest ->
                val apiList = rest.getOrDefault("apiList", null)
                if (apiList != null && apiList is Collection<*>) {
                    apiList.forEach { api ->
                        if (api is Map<*, *>) {
                            val path = api.getOrDefault("apis", null)
                            if (path != null) {
                                verifyApisSet.add(path.toString().trim())
                            }
                        }
                    }
                }
            }
        }
        return verifyApisSet.contains(api.trim())
    }

    /**
     * 执行验证
     *
     * 1. 通过用户ID获取用户的角色
     * 2. 通过角色获取用户的权限
     * 3. 通过权限获取用的API列表
     * 4. 对比当前访问的API是否在自己的权限API列表内
     * 5. 拦截或者放行
     */
    fun doVerify(api: String, userId: String): Boolean {
        val apis = permissionApiMapper.selectUserApis(userId)
        if (apis.isEmpty()) {
            return false
        }
        return apis.contains(api)
    }
}
