package com.fqchildren.oa

import com.fqchildren.oa.service.InitService
import com.fqchildren.oa.utils.PassUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class OaApplicationTests {

    @Autowired
    lateinit var initService: InitService

    @Test
    fun contextLoads() {
    }

}
