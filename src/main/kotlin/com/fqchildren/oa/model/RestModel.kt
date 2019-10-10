package com.fqchildren.oa.model

import com.fqchildren.oa.base.BaseModel
import org.slf4j.LoggerFactory

/**
 * REST类型控制器返回数据模型
 */
data class RestModel(
        var code: Int = 0,
        var msg: String = "success",
        var data: Any? = null
) : BaseModel() {

    fun success(): RestModel {
        this.code = 0
        this.msg = "success"
        return this
    }

    fun failure(): RestModel {
        this.code = Int.MIN_VALUE
        this.msg = "failure"
        return this
    }

    fun withLog(obj: Any = "", method: String = ""): RestModel {
        log.warn("对象：${obj.javaClass.name} & 方法：$method => code：${this.code} & msg：${this.msg}")
        return this
    }

    companion object {
        private val log = LoggerFactory.getLogger(RestModel::class.java)

        fun success(): RestModel {
            return RestModel(code = 0, msg = "success");
        }

        fun failure(): RestModel {
            return RestModel(code = Int.MIN_VALUE, msg = "failure");
        }
    }
}
