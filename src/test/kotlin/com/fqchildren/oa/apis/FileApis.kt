package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

class FileApis : BaseApis() {

    @Test
    fun logger() {
        show(this, RestCore.json("/file/logger.sel"))
    }

    @Test
    fun loggerDelete() {
        show(this, RestCore.json("/file/loggerDelete.do", mapOf("path" to "D:\\oa-assets\\run-logs\\2019-08-27.log")))
    }
}
