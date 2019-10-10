package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.service.RoleService
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

@ApiClass(value = "角色管理")
@RestController
@RequestMapping(value = ["/role"], method = [RequestMethod.POST])
class RoleRest : BaseRest {

    @Autowired
    private lateinit var roleInfoMapper: RoleInfoMapper

    @Autowired
    private lateinit var rolePermissionMapper: RolePermissionMapper

    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var roleService: RoleService

    @Permission
    @ApiMethod(value = "查询角色", depict = "获取系统中目前的存在的所有角色")
    @RequestMapping(value = ["/role.sel"])
    fun select(@RequestBody params: ParamModel): RestModel {
        return RestModel(data = roleInfoMapper.list(RoleInfo().sql()))
    }

    @Permission
    @ApiMethod(value = "添加角色",
            depict = "为系统新增一个角色",
            params = [
                ApiParam(value = "name", depict = "角色名称"),
                ApiParam(value = "depict", depict = "角色描述")
            ])
    @RequestMapping(value = ["/role.ins"])
    fun insert(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("name", "depict")) {
            return params.fail()
        }
        val name = params.string("name")
        val depict = params.string("depict")

        val roleInfo = RoleInfo()
        roleInfo.name = name
        roleInfo.depict = depict
        roleInfoMapper.insert(roleInfo)

        return RestModel(data = roleInfo)
    }

    @Permission
    @ApiMethod(value = "修改角色",
            depict = "修改一个角色的信息，注意：不需要修改的字段需要原样返回",
            params = [
                ApiParam(value = "uid", depict = "当前角色ID"),
                ApiParam(value = "name", depict = "角色名称"),
                ApiParam(value = "depict", depict = "角色描述")
            ])
    @RequestMapping(value = ["/role.upd"])
    fun update(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid", "name", "depict")) {
            return params.fail()
        }
        val uid = params.string("uid")
        val name = params.string("name")
        val depict = params.string("depict")

        val roleInfo = roleInfoMapper.select(RoleInfo().sql(uid = uid))
                ?: return RestModel(code = -2, msg = "修改的角色不存在，修改失败")
        roleInfo.name = name
        roleInfo.depict = depict
        roleInfoMapper.update(roleInfo)

        return RestModel(data = roleInfo)
    }

    @Permission
    @ApiMethod(value = "删除角色",
            depict = "删除一个角色，同时相关的子角色也会被删除，已经引用了这个角色的用户也会被更新,返回受影响的数据统计",
            params = [
                ApiParam(value = "uid", depict = "当前角色ID")
            ])
    @RequestMapping(value = ["/role.del"])
    fun delete(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid")) {
            return params.fail()
        }
        val uid = params.string("uid")
        return RestModel(data = roleService.deleteRole(uid))
    }

    @Permission
    @ApiMethod(value = "绑定权限",
            depict = "更新角色权限集合，一个角色ID可以绑定多个权限ID，更新时对于不修改的权限也要跟着提交",
            params = [
                ApiParam(value = "roleId", depict = "角色ID"),
                ApiParam(value = "perIds",
                        depict = "权限ID列表，多个用 & 隔开，空字符串代表清空权限",
                        example = "v1&v2&v3")
            ])
    @RequestMapping(value = ["/permission.upd"])
    fun permissionUpd(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("roleId")) {
            return params.fail()
        }
        val roleId = params.string("roleId")
        val perIds = params.string("perIds")
        return RestModel(data = roleService.bindingPermission(roleId, perIds))
    }

    @Permission
    @ApiMethod(value = "角色权限",
            depict = "查询角色拥有的权限列表",
            params = [
                ApiParam(value = "roleId", depict = "角色ID")
            ])
    @RequestMapping(value = ["/permission.sel"])
    fun permissionSel(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("roleId")) {
            return params.fail()
        }
        val roleId = params.string("roleId")
        return RestModel(data = rolePermissionMapper.list(RolePermission(roleId = roleId).sql()))
    }

    @Permission
    @ApiMethod(value = "角色用户", depict = "当前角色被多少个用户引用",
            params = [
                ApiParam(value = "roleId", depict = "角色ID")
            ])
    @RequestMapping(value = ["/user.sel"])
    fun user(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("roleId")) {
            return params.fail()
        }
        val roleId = params.string("roleId")
        return RestModel(data = userRoleMapper.selectRoleUsers(roleId))
    }

    @Permission
    @ApiMethod(value = "删除用户", depict = "删除这个角色的用户",
            params = [
                ApiParam(value = "uid", depict = "关联ID")
            ])
    @RequestMapping(value = ["/user.del"])
    fun userDel(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid")) {
            return params.fail()
        }
        val uid = params.string("uid")
        return RestModel(data = userRoleMapper.delete(UserRole().sql(uid = uid)))
    }

    @Permission
    @ApiMethod(value = "添加用户", depict = "添加这个角色的用户",
            params = [
                ApiParam(value = "userId", depict = "用户ID"),
                ApiParam(value = "roleId", depict = "角色ID")
            ])
    @RequestMapping(value = ["/user.ins"])
    fun userIns(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("userId", "roleId")) {
            return params.fail()
        }
        val userId = params.string("userId")
        val roleId = params.string("roleId")

        val userInfo = userInfoMapper.select(UserInfo().sql(uid = userId))
                ?: return RestModel(code = -1, msg = "指定的用户不存在")
        val roleInfo = roleInfoMapper.select(RoleInfo().sql(uid = roleId))
                ?: return RestModel(code = -2, msg = "指定的角色不存在")

        val userRole = UserRole()
        userRole.userId = userInfo.uid
        userRole.roleId = roleInfo.uid

        return RestModel(data = userRoleMapper.insert(userRole))
    }
}
