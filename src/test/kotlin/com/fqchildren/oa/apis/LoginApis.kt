package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

class LoginApis : BaseApis() {

    @Test
    fun exist() {
        val params = mapOf(
                "phone" to "12345678900"
        )
        show(this, RestCore.json("/login/exist.sel", params))
    }

    @Test
    fun account() {
        val params = mapOf(
                "username" to "12345678900",    // admin
                "password" to "38180678"
        )
        show(this, RestCore.json("/login/account.do", params))
    }

    @Test
    fun wxGzhId() {
        val params = mapOf(
                "openId" to "12345678900"
        )
        show(this, RestCore.json("/login/wxGzhId.do", params))
    }

    @Test
    fun register() {
        val params = mapOf(
                "phone" to "12345678901",
                "code" to "123456",
                "password" to "123456"
        )
        show(this, RestCore.json("/login/register.do", params))
    }

    @Test
    fun binding() {
        val params = mapOf(
                "phone" to "12345678901",
                "code" to "123456",
                "openId" to "123456"
        )
        show(this, RestCore.json("/login/binding.do", params))
    }

    @Test
    fun password() {
        val params = mapOf(
                "password" to "000000"
        )
        show(this, RestCore.json("/login/password.upd", params))
    }

    @Test
    fun refresh() {
        val params = mapOf(
                "userId" to "100000"
        )
        show(this, RestCore.json("/login/refresh.do", params))
    }

}
