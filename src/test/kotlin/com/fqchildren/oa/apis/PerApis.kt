package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test
import java.net.URLEncoder

class PerApis : BaseApis() {
    @Test
    fun select() {
        show(this, RestCore.json("/per/per.sel"))
    }

    @Test
    fun insert() {
        val params = mapOf(
                "name" to "财务接口",
                "depict" to "汇总财务相关的接口"
        )
        show(this, RestCore.json("/per/per.ins", params))
    }

    @Test
    fun update() {
        val params = mapOf(
                "uid" to "1301187018096679",
                "name" to "财务接口2",
                "depict" to "汇总财务相关的接口2"
        )
        show(this, RestCore.json("/per/per.upd", params))
    }

    @Test
    fun delete() {
        val params = mapOf(
                "uid" to "1301187018096679"
        )
        show(this, RestCore.json("/per/per.del", params))
    }

    @Test
    fun apisUpd() {
        fun ue(value: String): String {
            val data = value.replace("|", "").replace("&", "")
            return URLEncoder.encode(data, "UTF-8")
        }

        val apiData = "${ue("修改部门")}|${ue("修改一个部门的信息，注意：不需要修改的字段需要原样返回")}|${ue("/dep/dep.upd")}" +
                "&${ue("修改权限")}|${ue("修改一个权限的信息，注意：不需要修改的字段需要原样返回")}|${ue("/per/per.upd")}" +
                "&${ue("修改角色")}|${ue("修改一个角色的信息，注意：不需要修改的字段需要原样返回")}|${ue("/role/role.upd")}"
        val params = mapOf(
                "perId" to "100000",
                "apiData" to apiData
        )
        show(this, RestCore.json("/per/apis.upd", params))
    }

    @Test
    fun apisSel() {
        val params = mapOf(
                "perId" to "100000"
        )
        show(this, RestCore.json("/per/apis.sel", params))
    }
}
