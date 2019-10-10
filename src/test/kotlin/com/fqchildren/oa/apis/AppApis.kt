package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

//@RunWith(SpringRunner::class)
//@SpringBootTest
class AppApis : BaseApis() {

    @Test
    fun init() {
        show(this, RestCore.json("/app/init.do"))
    }

    @Test
    fun apis() {
        show(this, RestCore.json("/app/apis.sel"))
    }

}
