package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RequestModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.service.DepService
import com.fqchildren.oa.table.*
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import com.fqchildren.oa.utils.permission.Permission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@ApiClass(value = "部门管理")
@RestController
@RequestMapping(value = ["/dep"], method = [RequestMethod.POST])
class DepRest : BaseRest {

    @Autowired
    private lateinit var depService: DepService

    @Autowired
    private lateinit var departmentInfoMapper: DepartmentInfoMapper

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var regionInfoMapper: RegionInfoMapper

    companion object {
        private const val per_code = -403
        private const val per_msg = "权限不足，你不能在当前部门添加子部门"
    }

    @Permission
    @ApiMethod(value = "部门列表", depict = "获取系统中目前的存在的所有部门，前端需要根据权限做显示控制")
    @RequestMapping(value = ["/dep.sel"])
    fun select(@RequestBody params: ParamModel): RestModel {
        val depData = mutableSetOf<Any>()
        val departmentInfoList = departmentInfoMapper.list(DepartmentInfo().sql())
        departmentInfoList.forEach { dep ->
            depData.add(depService.depInfo(dep))
        }
        return RestModel(data = depData)
    }

    @Permission
    @ApiMethod(value = "添加部门",
            depict = "添加一个部门必须指定一个上级部门，同时必须是自己能管理的部门",
            params = [
                ApiParam(value = "pid", depict = "上级部门ID"),
                ApiParam(value = "name", depict = "部门名称"),
                ApiParam(value = "code", depict = "部门代码"),
                ApiParam(value = "adminUser", depict = "管理员用户ID", required = false),
                ApiParam(value = "regionId", depict = "部门所在地区ID", required = false)
            ])
    @RequestMapping(value = ["/dep.ins"])
    fun insert(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("pid", "name", "code")) {
            return params.fail()
        }
        val userId = requestModel.token.uid
        val pid = params.string("pid")
        val name = params.string("name")
        val code = params.string("code")
        val adminUser = params.string("adminUser")
        val regionId = params.string("regionId")

        if (!depService.checkUserPermission(userId, pid)) {
            return RestModel(code = per_code, msg = per_msg)
        }

        val departmentInfo = DepartmentInfo()
        departmentInfo.pid = pid
        departmentInfo.name = name
        departmentInfo.code = code
        departmentInfo.adminUser = if (adminUser.isEmpty()) userId else adminUser
        departmentInfo.regionId = regionId
        departmentInfoMapper.insert(departmentInfo)

        return RestModel(data = depService.depInfo(departmentInfo))
    }

    @Permission
    @ApiMethod(value = "修改部门",
            depict = "修改一个部门的信息，注意：不需要修改的字段需要原样返回",
            params = [
                ApiParam(value = "uid", depict = "当前部门ID"),
                ApiParam(value = "pid", depict = "上级部门ID"),
                ApiParam(value = "name", depict = "部门名称"),
                ApiParam(value = "code", depict = "部门代码"),
                ApiParam(value = "adminUser", depict = "管理员用户ID", required = false),
                ApiParam(value = "regionId", depict = "部门所在地区ID", required = false)
            ])
    @RequestMapping(value = ["/dep.upd"])
    fun update(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("uid", "pid", "name", "code")) {
            return params.fail()
        }
        val userId = requestModel.token.uid
        val uid = params.string("uid")
        val pid = params.string("pid")
        val name = params.string("name")
        val code = params.string("code")
        val adminUser = params.string("adminUser")
        val regionId = params.string("regionId")

        if (!depService.checkUserPermission(userId, uid)) {
            return RestModel(code = per_code, msg = per_msg)
        }

        departmentInfoMapper.select(DepartmentInfo()
                .sql(uid = pid)) ?: return RestModel(code = -1, msg = "指定的上级部门不存在")
        val cDep = departmentInfoMapper.select(DepartmentInfo()
                .sql(uid = uid)) ?: return RestModel(code = -2, msg = "修改的部门不存在")
        cDep.pid = pid
        cDep.name = name
        cDep.code = code
        cDep.adminUser = adminUser
        cDep.regionId = regionId
        departmentInfoMapper.update(cDep)

        return RestModel(data = depService.depInfo(cDep))
    }

    @Permission
    @ApiMethod(value = "删除部门",
            depict = "删除一个部门，同时相关的子部门也会被删除，部门用户都会变成无部门的用户,返回受影响的数据统计",
            params = [
                ApiParam(value = "uid", depict = "当前部门ID")
            ])
    @RequestMapping(value = ["/dep.del"])
    fun delete(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("uid")) {
            return params.fail()
        }
        val userId = requestModel.token.uid
        val uid = params.string("uid")

        if (!depService.checkUserPermission(userId, uid)) {
            return RestModel(code = per_code, msg = per_msg)
        }
        return RestModel(data = depService.deleteDepartment(uid))
    }

    @Permission
    @ApiMethod(value = "部门用户", depict = "查询指定部门下的所有用户列表",
            params = [
                ApiParam(value = "depId", depict = "部门ID")
            ])
    @RequestMapping(value = ["/user.sel"])
    fun user(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("depId")) {
            return params.fail()
        }
        val depId = params.string("depId")
        val userList = userInfoMapper.list(UserInfo(depId = depId).sql())
        return RestModel(data = userList)
    }

    @Permission
    @ApiMethod(value = "删除用户", depict = "删除指定部门下的用户",
            params = [
                ApiParam(value = "depId", depict = "部门ID"),
                ApiParam(value = "userId", depict = "用户ID")
            ])
    @RequestMapping(value = ["/user.del"])
    fun userDel(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("depId", "userId")) {
            return params.fail()
        }
        val depId = params.string("depId")
        val userId = params.string("userId")

        if (!depService.checkUserPermission(requestModel.token.uid, depId)) {
            return RestModel(code = per_code, msg = per_msg)
        }

        val userInfo = userInfoMapper.select(UserInfo().sql(uid = userId))
                ?: return RestModel(code = -1, msg = "用户信息错误")
        if (userInfo.depId != depId) {
            return RestModel(code = -2, msg = "用户部门错误")
        }
        userInfo.depId = "" // 清空用户的部门信息,取消掉与部门关联
        userInfoMapper.update(userInfo)
        return RestModel(data = userInfo)
    }

    @Permission
    @ApiMethod(value = "添加用户(UID)", depict = "添加指定部门下的用户",
            params = [
                ApiParam(value = "depId", depict = "部门ID"),
                ApiParam(value = "userId", depict = "用户ID")
            ])
    @RequestMapping(value = ["/user.ins"])
    fun userIns(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("depId", "userId")) {
            return params.fail()
        }
        val depId = params.string("depId")
        val userId = params.string("userId")

        if (!depService.checkUserPermission(requestModel.token.uid, depId)) {
            return RestModel(code = per_code, msg = per_msg)
        }

        val userInfo = userInfoMapper.select(UserInfo().sql(uid = userId))
                ?: return RestModel(code = -1, msg = "用户信息错误")
        if (userInfo.depId == depId) {
            return RestModel(data = userInfo)
        }
        if (userInfo.depId.isNotEmpty()) {
            val departmentInfo = departmentInfoMapper.select(DepartmentInfo().sql(uid = userInfo.depId))
                    ?: return RestModel(code = -2, msg = "部门信息错误")
            return RestModel(code = -3,
                    msg = "用户已经绑定到${departmentInfo.name}部门,请先去该部门解绑用户", data = departmentInfo)
        }
        userInfo.depId = depId // 绑定用户部门
        userInfoMapper.update(userInfo)
        return RestModel(data = userInfo)
    }

    @Permission
    @ApiMethod(value = "添加用户(电话)", depict = "添加指定部门下的用户,通过电话号码",
            params = [
                ApiParam(value = "depId", depict = "部门ID"),
                ApiParam(value = "phone", depict = "用户电话")
            ])
    @RequestMapping(value = ["/user.do"])
    fun userInsInPhone(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("depId", "phone")) {
            return params.fail()
        }
        val phone = params.string("phone")
        val userInfo = userInfoMapper.select(UserInfo(phone = phone).sql())
                ?: return RestModel(code = -101, msg = "用户不存在")
        params["userId"] = userInfo.uid
        return this.userIns(params, requestModel) // 引用ID的方法处理
    }

    @Permission
    @ApiMethod(value = "设置管理员", depict = "设置指定用户为部门管理员",
            params = [
                ApiParam(value = "depId", depict = "部门ID"),
                ApiParam(value = "userId", depict = "用户ID")
            ])
    @RequestMapping(value = ["/admin.do"])
    fun admin(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        if (!params.verify("depId", "userId")) {
            return params.fail()
        }
        val depId = params.string("depId")
        val userId = params.string("userId")

        if (!depService.checkUserPermission(requestModel.token.uid, depId)) {
            return RestModel(code = per_code, msg = per_msg)
        }

        val userInfo = userInfoMapper.select(UserInfo().sql(uid = userId))
                ?: return RestModel(code = -1, msg = "用户信息错误")
        if (userInfo.depId != depId) {
            return RestModel(code = -2, msg = "用户部门错误,用户必须在当前部门下才能作为管理员")
        }
        val departmentInfo = departmentInfoMapper.select(DepartmentInfo().sql(uid = depId))
                ?: return RestModel(code = -3, msg = "部门信息错误")
        departmentInfo.adminUser = userInfo.uid
        departmentInfoMapper.update(departmentInfo)
        return RestModel(data = departmentInfo)
    }

}
