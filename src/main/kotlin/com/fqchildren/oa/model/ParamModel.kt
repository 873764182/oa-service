package com.fqchildren.oa.model

import org.springframework.validation.support.BindingAwareModelMap

/**
 * 参数模型
 */
class ParamModel : BindingAwareModelMap() {

    private var verifyKeys: List<String> = listOf()

    fun string(key: String, default: String = ""): String {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString()
        } catch (e: Exception) {
            return default
        }
    }

    fun float(key: String, default: Float = 0F): Float {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString().toFloat()
        } catch (e: Exception) {
            return default
        }
    }

    fun int(key: String, default: Int = 0): Int {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString().toDouble().toInt()
        } catch (e: Exception) {
            return default
        }
    }

    fun long(key: String, default: Long = 0L): Long {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString().toDouble().toLong()
        } catch (e: Exception) {
            return default
        }
    }

    fun double(key: String, default: Double = 0.0): Double {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString().toDouble()
        } catch (e: Exception) {
            return default
        }
    }

    fun boolean(key: String, default: Boolean = false): Boolean {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString().toBoolean()
        } catch (e: Exception) {
            return default
        }
    }

    fun list(key: String, default: List<Any> = listOf()): List<*> {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value == null) {
                value = default
            }
            return value as List<*>
        } catch (e: Exception) {
            return default
        }
    }

    fun any(key: String, default: Any? = null): Any? {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value == null || value.toString().isEmpty() || value.toString() == "null") {
                value = default
            }
            return value
        } catch (e: Exception) {
            return default
        }
    }

    fun verify(vararg keys: String): Boolean {
        this.verifyKeys = keys.toList()
        var flag = true
        keys.forEach {
            val data = any(it)
            if (data == null) {
                flag = false
            }
        }
        return flag
    }

    fun fail(code: Int = -999, msg: String? = null): RestModel {
        return RestModel(code = code, msg = msg ?: "the required parameter $verifyKeys cannot be empty")
    }

    // =================================================================================================================

    /**
     * 默认处理SQL需要的页数下标参数
     */
    fun sqlPage(key: String = "page", default: Int = 0): Int {
        val limit = this.sqlLimit()
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return (default * limit)
            if (value.toString().isEmpty()) {
                value = default
            }
            return (value.toString().toInt() * limit)
        } catch (e: Exception) {
            return (default * limit)
        }
    }

    /**
     * 默认处理SQL需要的页数大小参数
     */
    fun sqlLimit(key: String = "limit", default: Int = 10): Int {
        try {
            var value: Any? = super.getOrDefault(key, default) ?: return default
            if (value.toString().isEmpty()) {
                value = default
            }
            return value.toString().toDouble().toInt()
        } catch (e: Exception) {
            return default
        }
    }

    /**
     * 默认处理SQL需要的搜索关键字参数
     */
    fun sqlSearch(key: String = "search", default: String = ""): String {
        return this.string(key, default)
    }

    /**
     * 默认处理SQL需要的开始时间参数
     */
    fun sqlSt(key: String = "st", default: Long = 0): Long {
        return this.long(key, default)
    }

    /**
     * 默认处理SQL需要的结束时间参数
     */
    fun sqlEt(key: String = "et", default: Long = System.currentTimeMillis()): Long {
        return this.long(key, default)
    }

}
