package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

class UserApis : BaseApis() {

    @Test
    fun resolve() {
        val params = mapOf(
                "password" to "C4EB5DF0250484448DB59E10C97261E4"
        )
        show(this, RestCore.json("/user/resolve.sel", params))
    }

    @Test
    fun select() {
        val params = mapOf(
                "search" to "",
                "st" to 0,
                "et" to System.currentTimeMillis(),
                "page" to 0,
                "limit" to 10
        )
        show(this, RestCore.json("/user/user.sel", params))
    }

    @Test
    fun insert() {
        val params = mapOf(
                "phone" to "12345678901",
                "email" to "",
                "wxOid" to "",
                "username" to "测试",
                "password" to "123456",
                "photo" to "",
                "gender" to "",
                "regionId" to "",
                "depId" to ""
        )
        show(this, RestCore.json("/user/user.ins", params))
    }

    @Test
    fun update() {
        val params = mapOf(
                "uid" to "1301747268059207",
                "phone" to "12345678901",
                "email" to "123456@qq.com",
                "wxOid" to "",
                "username" to "测试",
                "password" to "123456",
                "photo" to "",
                "gender" to "",
                "regionId" to "",
                "depId" to ""
        )
        show(this, RestCore.json("/user/user.upd", params))
    }

    @Test
    fun delete() {
        val params = mapOf(
                "uid" to "1301747268059207"
        )
        show(this, RestCore.json("/user/user.del", params))
    }

    @Test
    fun roleSel() {
        val params = mapOf(
                "userId" to "100000"
        )
        show(this, RestCore.json("/user/role.sel", params))
    }

    @Test
    fun roleUpd() {
        val params = mapOf(
                "userId" to "100000",
                "roleIds" to "100000&2222222"
        )
        show(this, RestCore.json("/user/role.upd", params))
    }

    @Test
    fun info() {
        val params = mapOf(
                "uid" to "100000",
                "phone" to "100000&2222222"
        )
        show(this, RestCore.json("/user/info.sel", params))
    }
}
