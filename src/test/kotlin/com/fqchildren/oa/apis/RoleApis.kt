package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

class RoleApis : BaseApis() {

    @Test
    fun select() {
        show(this, RestCore.json("/role/role.sel"))
    }

    @Test
    fun insert() {
        val params = mapOf(
                "pid" to "100000",
                "name" to "财务",
                "depict" to "管理财务人员，可以访问财务相关权限"
        )
        show(this, RestCore.json("/role/role.ins", params))
    }

    @Test
    fun update() {
        val params = mapOf(
                "uid" to "1301180193964071",
                "pid" to "100000",
                "name" to "财务2",
                "depict" to "管理财务人员，可以访问财务相关权限2"
        )
        show(this, RestCore.json("/role/role.upd", params))
    }

    @Test
    fun delete() {
        val params = mapOf(
                "uid" to "1301180193964071"
        )
        show(this, RestCore.json("/role/role.del", params))
    }

    @Test
    fun permissionUpd() {
        val params = mapOf(
                "roleId" to "100000",
                "perIds" to "100000&111111&222222"
        )
        show(this, RestCore.json("/role/permission.upd", params))
    }

    @Test
    fun permissionSel() {
        val params = mapOf(
                "roleId" to "100000"
        )
        show(this, RestCore.json("/role/permission.sel", params))
    }

    @Test
    fun user() {
        val params = mapOf(
                "roleId" to "100000"
        )
        show(this, RestCore.json("/role/user.sel", params))
    }

    @Test
    fun userDel() {
        val params = mapOf(
                "uid" to "100000"
        )
        show(this, RestCore.json("/role/user.del", params))
    }

    @Test
    fun userIns() {
        val params = mapOf(
                "userId" to "100000",
                "roleId" to "100000"
        )
        show(this, RestCore.json("/role/user.ins", params))
    }
}
