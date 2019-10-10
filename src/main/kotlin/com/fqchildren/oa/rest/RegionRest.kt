package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.table.RegionInfo
import com.fqchildren.oa.table.RegionInfoMapper
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@ApiClass(value = "地区管理")
@RestController
@RequestMapping(value = ["/region"], method = [RequestMethod.POST])
class RegionRest : BaseRest {

    @Autowired
    private lateinit var regionInfoMapper: RegionInfoMapper

    @ApiMethod(value = "获取地区", depict = "根绝PID获取信息",
            params = [
                ApiParam(value = "pid", depict = "上级ID,根节点请传100000即可")
            ])
    @RequestMapping(value = ["/region.sel"])
    fun regionSel(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("pid")) {
            return params.fail()
        }
        val pid = params.string("pid")
        val regionList = regionInfoMapper.list(RegionInfo(pid = pid).sql())
        return RestModel(data = regionList)
    }

}