package com.fqchildren.oa.base

import org.apache.ibatis.annotations.*
import org.apache.ibatis.jdbc.SQL


/**
 * 自动生成增删查改操作，有一定的约定规则
 */
interface BaseMapper<T> : java.io.Serializable {

    companion object {
        private const val KEY_ID = "uid"
        private const val KEY_TIME = "time"

        // 是否是合法的父对象属性
        fun isSuperclassField(fieldName: String): Boolean {
            if (fieldName == KEY_ID || fieldName == KEY_TIME) {
                return true
            }
            return false
        }

        // 对象属性是否为空，对 Boolean 类型判断会失误，请独立书写SQL语句
        fun isEmpty(name: String, type: Class<*>, value: Any?): Boolean {
            val vs = value.toString()
            return !(value != null && vs.isNotEmpty() && vs != "null" && vs != "0.0" && vs != "0")
        }
    }

    @InsertProvider(type = SqlFactory::class, method = "insert")
    fun insert(data: T): Int = 0

    @DeleteProvider(type = SqlFactory::class, method = "delete")
    fun delete(data: T): Int = 0

    @Results(value = [
        (Result(property = KEY_ID, column = KEY_ID)),
        (Result(property = KEY_TIME, column = KEY_TIME))
    ])
    @SelectProvider(type = SqlFactory::class, method = "select")
    fun select(data: T): T? = null

    @Results(value = [
        (Result(property = KEY_ID, column = KEY_ID)),
        (Result(property = KEY_TIME, column = KEY_TIME))
    ])
    @SelectProvider(type = SqlFactory::class, method = "list")
    fun list(data: T,
             st: Long = 0, et: Long = System.currentTimeMillis(),
             page: Int = 0, limit: Int = Int.MAX_VALUE, order: String = "DESC"): LinkedHashSet<T> = linkedSetOf()

    @Results(value = [
        (Result(property = KEY_ID, column = KEY_ID)),
        (Result(property = KEY_TIME, column = KEY_TIME))
    ])
    @SelectProvider(type = SqlFactory::class, method = "like")
    fun like(data: T): LinkedHashSet<T> = linkedSetOf()

    @Results(value = [
        (Result(property = KEY_ID, column = KEY_ID)),
        (Result(property = KEY_TIME, column = KEY_TIME))
    ])
    @SelectProvider(type = SqlFactory::class, method = "inUid")
    fun inUid(data: T, uidList: Set<String> = setOf()): LinkedHashSet<T> = linkedSetOf()

    @UpdateProvider(type = SqlFactory::class, method = "update")
    fun update(data: T): Int = 0

    @SelectProvider(type = SqlFactory::class, method = "count")
    fun count(data: T, st: Long = 0, et: Long = System.currentTimeMillis()): Long = 0

    class SqlFactory {

        fun insert(data: Any): String {
            val keyList = keyList(data.javaClass)
            val sql = SQL()
            sql.INSERT_INTO(humpToUnderline(data.javaClass.simpleName))
            keyList.forEach {
                sql.VALUES(it.trim(), "#{${it.trim()}}")
            }
            return sql.toString()
        }

        fun delete(data: Any): String {
            val cls = data.javaClass
            val sql = SQL()
            sql.DELETE_FROM(humpToUnderline(cls.simpleName))
            val keyList = keyList(cls)
            keyList.forEach {
                val field = if (isSuperclassField(it)) {
                    cls.superclass.getDeclaredField(it.trim())
                } else {
                    cls.getDeclaredField(it.trim())
                }
                field.isAccessible = true
                val value = field.get(data)
                if (!isEmpty(field.name, field.type, value)) {
                    sql.WHERE("${it.trim()} = #{${it.trim()}}")
                }
            }
            return sql.toString()
        }

        fun select(data: Any): String {
            val cls = data.javaClass
            val sql = SQL()
            sql.FROM(humpToUnderline(cls.simpleName))
            val keyList = keyList(cls)
            keyList.forEach {
                sql.SELECT(it.trim())

                val field = if (isSuperclassField(it)) {
                    cls.superclass.getDeclaredField(it.trim())
                } else {
                    cls.getDeclaredField(it.trim())
                }
                field.isAccessible = true
                val value = field.get(data)
                if (!isEmpty(field.name, field.type, value)) {
                    sql.WHERE("${it.trim()} = #{${it.trim()}}")
                }
            }
            return sql.toString()
        }

        fun update(data: Any): String {
            val keyList = keyList(data.javaClass)
            val sql = SQL()
            sql.UPDATE(humpToUnderline(data.javaClass.simpleName))
            keyList.forEach {
                if (it != KEY_ID && it != KEY_TIME) {  // 主键与插入时间不能被更新
                    sql.SET("${it.trim()} = #{${it.trim()}}")
                }
            }
            sql.WHERE("$KEY_ID = #{$KEY_ID}")
            return sql.toString()
        }

        fun count(data: Any, st: Long = 0, et: Long = System.currentTimeMillis()): String {
            val cls = data.javaClass
            val sql = SQL()
            sql.FROM(humpToUnderline(cls.simpleName))
            sql.SELECT("COUNT(*)")
            if (st > 0 && et != System.currentTimeMillis()) {
                sql.WHERE("($KEY_TIME BETWEEN $st AND $et)")
            }
            return sql.toString()
        }

        fun list(data: Any,
                 st: Long = 0, et: Long = System.currentTimeMillis(),
                 page: Int = 0, limit: Int = Int.MAX_VALUE, order: String = "DESC"): String {
            val cls = data.javaClass
            val keyList = keyList(cls)
            val sql = SQL()
            sql.FROM(humpToUnderline(data.javaClass.simpleName))
            keyList.forEach {
                sql.SELECT(it.trim())

                val field = if (isSuperclassField(it)) {
                    cls.superclass.getDeclaredField(it.trim())
                } else {
                    cls.getDeclaredField(it.trim())
                }
                field.isAccessible = true
                val value = field.get(data)
                if (!isEmpty(field.name, field.type, value)) {
                    if (field.type.toString().toUpperCase().contains("STRING")) {
                        sql.WHERE("${it.trim()} = '$value'")
                    } else {
                        sql.WHERE("${it.trim()} = $value")
                    }
                }
            }
            if (st > 0 && et != System.currentTimeMillis()) {
                sql.WHERE("($KEY_TIME BETWEEN $st AND $et)")
            }
            return "$sql ORDER BY $KEY_TIME $order LIMIT $page, $limit"
        }

        fun like(data: Any): String {
            val cls = data.javaClass
            val sql = SQL()
            sql.FROM(humpToUnderline(cls.simpleName))
            val keyList = keyList(cls)
            val likeString = StringBuilder()
            keyList.forEach {
                sql.SELECT(it.trim())
                val field = if (isSuperclassField(it)) {
                    cls.superclass.getDeclaredField(it.trim())
                } else {
                    cls.getDeclaredField(it.trim())
                }
                field.isAccessible = true
                val value = field.get(data)
                if (!isEmpty(field.name, field.type, value) && field.type.toString().toUpperCase().contains("STRING")) {
                    likeString.append("($it LIKE '%$value%')").append(" OR ")   // 必须是String类型才能模糊查询
                }
            }
            var likeSql = likeString.toString()
            if (likeSql.endsWith(" OR ")) {
                likeSql = likeSql.substring(0, likeSql.length - 4)
            }
            if (likeSql.isNotEmpty()) {
                sql.WHERE(likeSql)
            }
            return sql.toString()
        }

        fun inUid(data: Any, uidList: Set<String> = setOf()): String {
            val cls = data.javaClass
            val sql = SQL()
            sql.FROM(humpToUnderline(cls.simpleName))
            val keyList = keyList(cls)
            keyList.forEach {
                sql.SELECT(it.trim())

                val field = if (isSuperclassField(it)) {
                    cls.superclass.getDeclaredField(it.trim())
                } else {
                    cls.getDeclaredField(it.trim())
                }
                field.isAccessible = true
                if (uidList.isNotEmpty()) {
                    val sb = StringBuilder()
                    uidList.forEach { id ->
                        sb.append(id.trim()).append(",")
                    }
                    var sbs = sb.toString()
                    if (sbs.endsWith(",")) {
                        sbs = sbs.substring(0, sbs.length - 1)
                    }
                    sql.WHERE("$KEY_ID IN ($sbs)")
                } else {
                    sql.WHERE("0 = 1")  // 永远为false的条件
                }
            }
            return sql.toString()
        }

        // 遍历出对象所有属性
        private fun keyList(cls: Class<*>): Set<String> {
            val keyList = mutableSetOf<String>()
            try {
                val superFields = cls.superclass.declaredFields
                superFields.forEach {
                    if (isSuperclassField(it.name)) {
                        it.isAccessible = true
                        keyList.add(it.name)
                    }
                }
            } catch (e: Exception) { /* 不存在父类 */
                e.printStackTrace()
            }
            val fields = cls.declaredFields
            fields.forEach {
                it.isAccessible = true
                keyList.add(it.name)
            }
            return keyList
        }

        // 获取对象对应的驼峰转下划线数据表名（要求标明与实体名必须按约定的方式定义）
        private fun humpToUnderline(className: String): String {
            val sb = StringBuilder(className)
            var temp = 0
            for (i in className.indices) {
                if (Character.isUpperCase(className[i])) {  // 找到大写字母，插入下划线
                    sb.insert(i + temp, "_")
                    temp += 1
                }
            }
            var result = sb.toString().toLowerCase()
            if (result.startsWith("_")) {
                result = result.substring(1, result.length)
            }
            return result
        }
    }

}
