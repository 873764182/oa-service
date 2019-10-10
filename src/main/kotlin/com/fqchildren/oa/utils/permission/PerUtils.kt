package com.fqchildren.oa.utils.permission

import com.fqchildren.oa.utils.ClassUtils
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 权限工具类（权限依赖于docs库）
 */
object PerUtils {
    private val mCacheMap = HashMap<String, Set<HashMap<String, Any>>>()    // 数据缓存

    private fun getClassApi(className: String): HashMap<String, Any>? {
        val cls = Class.forName(className)
        val isApi = cls.isAnnotationPresent(ApiClass::class.java)
        if (!isApi) {
            return null
        }
        val apiClass = cls.getAnnotation(ApiClass::class.java) ?: return null
        val clsMapper = cls.getAnnotation(RequestMapping::class.java) ?: return null

        val data = LinkedHashMap<String, Any>()
        data["rest"] = className
        data["name"] = apiClass.value

        val apis = LinkedHashSet<Any>()

        val methodList = cls.declaredMethods
        for (method in methodList) {
            val isMethod = method.isAnnotationPresent(ApiMethod::class.java)
            val isMapper = method.isAnnotationPresent(RequestMapping::class.java)
            val isPermission = method.isAnnotationPresent(Permission::class.java)
            if (!isMethod || !isMapper || !isPermission) {
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
            pathList.forEach {
                val api = mutableMapOf<String, Any>()
                api["apis"] = it
                api["name"] = am.value
                api["depict"] = am.depict
                apis.add(api)
            }
        }

        return if (apis.isEmpty()) {
            return null
        } else {
            data["apiList"] = apis
            data
        }
    }

    fun getApiData(packName: String, isChild: Boolean = false): Set<HashMap<String, Any>> {
        val cacheData = mCacheMap[packName + isChild]
        if (cacheData != null && cacheData is Set<*>) {
            return cacheData
        }

        val packageList = ClassUtils.getClassName(packName, isChild)
        if (packageList == null || packageList.isEmpty()) {
            return setOf()
        }
        val dataMap = LinkedHashSet<HashMap<String, Any>>()
        packageList.forEach {
            val apiData = getClassApi(it)
            if (apiData != null) {
                dataMap.add(apiData)
            }
        }

        mCacheMap[packName + isChild] = dataMap

        return dataMap
    }

}
