package com.fqchildren.oa

import com.fqchildren.oa.utils.PassUtils
import org.junit.Test

class KotlinTests {

    @Test
    fun adminPass() {
        println(PassUtils.encryptAes("38180678"))   // C4EB5DF0250484448DB59E10C97261E4
    }

}
