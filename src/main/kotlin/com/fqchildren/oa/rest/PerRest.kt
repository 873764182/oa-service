package com.fqchildren.oa.rest

import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.service.PerService
import com.fqchildren.oa.table.PermissionApi
import com.fqchildren.oa.table.PermissionApiMapper
import com.fqchildren.oa.table.PermissionInfo
import com.fqchildren.oa.table.PermissionInfoMapper
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import com.fqchildren.oa.utils.permission.Permission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@ApiClass(value = "权限管理")
@RestController
@RequestMapping(value = ["/per"], method = [RequestMethod.POST])
class PerRest {

    @Autowired
    private lateinit var permissionInfoMapper: PermissionInfoMapper

    @Autowired
    private lateinit var permissionApiMapper: PermissionApiMapper

    @Autowired
    private lateinit var perService: PerService

    @Permission
    @ApiMethod(value = "查询权限", depict = "获取系统中目前的存在的所有权限列表")
    @RequestMapping(value = ["/per.sel"])
    fun select(@RequestBody params: ParamModel): RestModel {
        return RestModel(data = permissionInfoMapper.list(PermissionInfo().sql()))
    }

    @Permission
    @ApiMethod(value = "添加权限",
            depict = "新增一个系统权限，权限新增后应该去绑定API接口才有存在意义",
            params = [
                ApiParam(value = "name", depict = "权限名称"),
                ApiParam(value = "depict", depict = "权限描述")
            ])
    @RequestMapping(value = ["/per.ins"])
    fun insert(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("name", "depict")) {
            return params.fail()
        }
        val name = params.string("name")
        val depict = params.string("depict")

        val permissionInfo = PermissionInfo()
        permissionInfo.name = name
        permissionInfo.depict = depict
        permissionInfoMapper.insert(permissionInfo)

        return RestModel(data = permissionInfo)
    }

    @Permission
    @ApiMethod(value = "修改权限",
            depict = "修改一个权限的信息，注意：不需要修改的字段需要原样返回",
            params = [
                ApiParam(value = "uid", depict = "当前角色ID"),
                ApiParam(value = "name", depict = "权限名称"),
                ApiParam(value = "depict", depict = "权限描述")
            ])
    @RequestMapping(value = ["/per.upd"])
    fun update(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid", "name", "depict")) {
            return params.fail()
        }
        val uid = params.string("uid")
        val name = params.string("name")
        val depict = params.string("depict")

        val permissionInfo = permissionInfoMapper.select(PermissionInfo()
                .sql(uid = uid)) ?: return RestModel(code = -1, msg = "修改的权限不存在，修改失败")
        permissionInfo.name = name
        permissionInfo.depict = depict
        permissionInfoMapper.update(permissionInfo)

        return RestModel(data = permissionInfo)
    }

    @Permission
    @ApiMethod(value = "删除权限",
            depict = "删除一个权限，同时相关的权限API引用也会被删除，返回受影响的数据统计",
            params = [
                ApiParam(value = "uid", depict = "当前权限ID")
            ])
    @RequestMapping(value = ["/per.del"])
    fun delete(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid")) {
            return params.fail()
        }
        val uid = params.string("uid")
        return RestModel(data = perService.deletePermission(uid))
    }

    @Permission
    @ApiMethod(value = "绑定接口",
            depict = "更新权限接口集合，一个权限可以绑定多个接口，更新时对于不修改的接口也要跟着提交",
            params = [
                ApiParam(value = "perId", depict = "权限ID"),
                ApiParam(value = "apiData",
                        depict = "接口列表，格式与顺序参看示例，拼接前先URL编码与清空掉内容里的关键字|&，空字符串代表清空接口",
                        example = "name1|depict1|api1&name2|depict2|api2&name3|depict3|api3")
            ])
    @RequestMapping(value = ["/apis.upd"])
    fun apisUpd(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("perId")) {
            return params.fail()
        }
        val perId = params.string("perId")
        val apiData = params.string("apiData")
        return RestModel(data = perService.bindingApis(perId, apiData))
    }

    @Permission
    @ApiMethod(value = "查询接口",
            depict = "查询当前权限拥有的接口列表",
            params = [
                ApiParam(value = "perId", depict = "权限ID")
            ])
    @RequestMapping(value = ["/apis.sel"])
    fun apisSel(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("perId")) {
            return params.fail()
        }
        val perId = params.string("perId")
        return RestModel(data = permissionApiMapper.list(PermissionApi(perId = perId).sql()))
    }

}
