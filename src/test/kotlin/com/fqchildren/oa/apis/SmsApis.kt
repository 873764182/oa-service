package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class SmsApis : BaseApis() {

    @Test
    fun code() {
        show(this, RestCore.json("/sms/code.do", mapOf(
                "phone" to "17687780079"
        )))
    }

}
