package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

class DepApis : BaseApis() {

    @Test
    fun select() {
        show(this, RestCore.json("/dep/dep.sel"))
    }

    @Test
    fun insert() {
        val params = mapOf(
                "pid" to "100000",
                "name" to "测试部门",
                "code" to "01",
                "adminUser" to "",
                "regionId" to ""
        )
        show(this, RestCore.json("/dep/dep.ins", params))
    }

    @Test
    fun update() {
        val params = mapOf(
                "uid" to "1301043797295303",
                "pid" to "100000",
                "name" to "测试部门",
                "code" to "01",
                "adminUser" to "100000",
                "regionId" to "100000"
        )
        show(this, RestCore.json("/dep/dep.upd", params))
    }

    @Test
    fun delete() {
        val params = mapOf(
                "uid" to "1301043797295303"
        )
        show(this, RestCore.json("/dep/dep.del", params))
    }

    @Test
    fun user() {
        val params = mapOf(
                "depId" to "100000"
        )
        show(this, RestCore.json("/dep/user.sel", params))
    }

    @Test
    fun userIns() {
        val params = mapOf(
                "depId" to "100000",
                "userId" to "1308463623831616"
        )
        show(this, RestCore.json("/dep/user.ins", params))
    }

    @Test
    fun userInsInPhone() {
        val params = mapOf(
                "depId" to "100000",
                "phone" to "12345678901"
        )
        show(this, RestCore.json("/dep/user.do", params))
    }

    @Test
    fun userDel() {
        val params = mapOf(
                "depId" to "100000",
                "userId" to "1308463623831616"
        )
        show(this, RestCore.json("/dep/user.del", params))
    }

    @Test
    fun admin() {
        val params = mapOf(
                "depId" to "100000",
                "userId" to "1308463623831616"
        )
        show(this, RestCore.json("/dep/admin.do", params))
    }
}
