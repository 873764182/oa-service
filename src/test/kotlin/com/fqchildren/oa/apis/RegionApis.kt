package com.fqchildren.oa.apis

import com.fqchildren.oa.core.BaseApis
import com.fqchildren.oa.core.RestCore
import org.junit.Test

class RegionApis : BaseApis() {

    @Test
    fun regionSel() {
        show(this, RestCore.json("/region/region.sel", mapOf("pid" to "100000")))
    }

}