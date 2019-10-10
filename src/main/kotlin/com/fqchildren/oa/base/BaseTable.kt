package com.fqchildren.oa.base

import com.fqchildren.oa.utils.UidUtils
import com.google.gson.GsonBuilder

/**
 * 数据库实体基础对象(数据表必须要有的字段)
 */
@Suppress("UNCHECKED_CAST")
open class BaseTable(
        var uid: String = newUid(),   // 唯一标识
        var time: Long = newTime()     // 创建时间
) : java.io.Serializable {

    /**
     * 数据库查询对象初始化
     */
    fun <T : BaseTable> sql(uid: String = "", time: Long = 0): T {
        this.uid = uid
        this.time = time
        return this as T
    }

    fun toJson(): String {
        return gson.toJson(this)
    }

    override fun equals(other: Any?): Boolean {
        return (other is BaseTable && other.uid == uid)
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }

    override fun toString(): String {
        return this.toJson()   // data 类会被系统重写,子类无法使用
    }

    companion object {
        private val gson = GsonBuilder().disableHtmlEscaping().disableInnerClassSerialization().create()

        fun newUid(): String {
            return UidUtils.nextId().toString()
        }

        fun newTime(): Long {
            return System.currentTimeMillis()
        }
    }
}
