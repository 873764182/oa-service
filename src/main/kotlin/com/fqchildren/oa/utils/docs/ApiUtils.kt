package com.fqchildren.oa.utils.docs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fqchildren.oa.utils.ClassUtils
import com.fqchildren.oa.utils.FunUtils
import com.fqchildren.oa.utils.permission.Permission
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URLEncoder


object ApiUtils {
    private val log = LoggerFactory.getLogger(ApiUtils::class.java)
    private val mCacheMap = HashMap<String, Any>()    // 数据缓存
    private var mResultMap: JSONObject? = null

    private fun getClassApi(className: String): Any? {
        val cls = Class.forName(className)
        val isApi = cls.isAnnotationPresent(ApiClass::class.java)
        if (!isApi) {
            return null
        }
        val apiClass = cls.getAnnotation(ApiClass::class.java) ?: return null
        val clsMapper = cls.getAnnotation(RequestMapping::class.java) ?: return null

        val data = LinkedHashMap<String, Any>()
        data["controller"] = className
        data["depict"] = apiClass.value
        // data["mappers"] = clsMapper.value
        // data["methods"] = clsMapper.method

        val apis = LinkedHashSet<Any>()

        val methodList = cls.declaredMethods
        for (method in methodList) {
            val isMethod = method.isAnnotationPresent(ApiMethod::class.java)
            val isMapper = method.isAnnotationPresent(RequestMapping::class.java)
            if (!isMethod || !isMapper) {
                continue
            }
            val am = method.getAnnotation(ApiMethod::class.java)
            val rm = method.getAnnotation(RequestMapping::class.java)

            val pathList = mutableListOf<String>()
            if (clsMapper.value.isNotEmpty() && rm.value.isNotEmpty()) {
                clsMapper.value.forEach { c ->
                    rm.value.forEach { m ->
                        pathList.add(c + m)    // 多个路径组合
                    }
                }
            }

            val api = mutableMapOf<String, Any>()
            api["path"] = pathList // clsMapper.value[0] + rm.value[0] // 只读第一个路径
            api["depict"] = am.value
            api["remark"] = am.depict
            api["method"] = rm.method
            api["verify"] = method.isAnnotationPresent(Permission::class.java) // 依赖权限模块的权限注解

            if (am.params.isNotEmpty()) {
                val params = LinkedHashSet<Any>()
                am.params.forEach {
                    val pm = mutableMapOf<String, Any>()
                    pm["name"] = it.value
                    pm["depict"] = it.depict
                    pm["required"] = it.required
                    pm["type"] = it.type
                    pm["example"] = it.example
                    params.add(pm)
                }
                api["params"] = params
            }

            // 处理响应结果
            if (am.result.isNotEmpty()) {
                val resultParams = LinkedHashSet<Any>()
                am.result.forEach {
                    val pm = mutableMapOf<String, Any>()
                    pm["name"] = it.value
                    pm["depict"] = it.depict
                    pm["required"] = it.required
                    pm["type"] = it.type
                    pm["example"] = it.example
                    resultParams.add(pm)
                }
                api["resultParams"] = resultParams
            }
            if (am.example.isNotEmpty()) {
                api["resultExample"] = am.example
            } else {
                api["resultExample"] = getResultExample(pathList)  // 从资源文件中获取
            }

            apis.add(api)
        }

        data["apis"] = apis

        return data
    }

    private fun getResultExample(pathList: List<String>, format: Boolean = false): String {
        fun getExample(list: List<String>): String {
            if (mResultMap == null) {
                val apiData = FunUtils.getClassPathFile("/data/docs/data.json")
                mResultMap = JSONObject(apiData)
            }
            list.forEach {
                try {
                    val temp = mResultMap?.get(it.trim())
                    if (temp != null) {
                        return temp.toString()
                    }
                } catch (e: Exception) {
                    log.error(e.message)
                }
            }
            return ""
        }

        var example = getExample(pathList)
        if (format) {
            example = example
                    .replace("\t", "&nbsp")
                    .replace("\n", "<br/>")
        }
        example = if (example.isEmpty() || example == "{}") {
            "{\"code\": 0, \"msg\": \"success\"}" // 返回默认值
        } else {
            "{\"code\": 0, \"msg\": \"success\", \"data\": $example}"
        }
        return example
    }

    private fun getHtmlTemplate(): String {
        return FunUtils.getClassPathFile("/data/docs/template.html")
    }

    fun getApiData(packName: String, isChild: Boolean = false): Set<*> {
        val cacheData = mCacheMap[packName + isChild]
        if (cacheData != null && cacheData is Set<*>) {
            return cacheData
        }

        val packageList = ClassUtils.getClassName(packName, isChild)
        if (packageList == null || packageList.isEmpty()) {
            return setOf<Any>()
        }
        val dataMap = LinkedHashSet<Any>()
        packageList.forEach {
            val apiData = getClassApi(it)
            if (apiData != null) {
                dataMap.add(apiData)
            }
        }

        mCacheMap[packName + isChild] = dataMap

        return dataMap
    }

    fun getApiDataWithHtml(
            packName: String, isChild: Boolean = false,
            docTitle: String = "", docDepict: String = "",
            publicParams: List<ApiPub> = listOf(), defPass: String = "123456",
            isZip: Boolean = true): String {
        var apiJsonData = ObjectMapper().writeValueAsString(getApiData(packName, isChild))
        if (apiJsonData.isNotEmpty()) {
            apiJsonData = apiJsonData.replace("\"", "\\\"")    // JSON引号问题
        }
        var htmlDoc = getHtmlTemplate()
                .replace("<%@{DOC-PASS}%>", URLEncoder.encode(defPass, "UTF-8"))
                .replace("<%@{DOC-PACK-NAME}%>", packName)
                .replace("<%@{DOC-TITLE}%>", docTitle)
                .replace("<%@{DOC-DEPICT}%>", docDepict)
                .replace("<%@{DOC-APIS-DATA}%>", apiJsonData)
        if (publicParams.isNotEmpty()) {
            var publicJsonData = ObjectMapper().writeValueAsString(publicParams)
            if (publicJsonData.isNotEmpty()) {
                publicJsonData = publicJsonData.replace("\"", "\\\"")    // JSON引号问题
            }
            htmlDoc = htmlDoc.replace("<%@{DOC-PUBLIC-PARAM}%>", publicJsonData)
        }
        return if (isZip) {
            htmlDoc.replace("  ", "")
        } else {
            htmlDoc
        }
    }
}
