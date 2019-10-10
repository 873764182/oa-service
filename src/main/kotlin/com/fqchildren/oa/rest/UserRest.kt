package com.fqchildren.oa.rest

import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.service.UserService
import com.fqchildren.oa.table.UserInfo
import com.fqchildren.oa.table.UserInfoMapper
import com.fqchildren.oa.table.UserRole
import com.fqchildren.oa.table.UserRoleMapper
import com.fqchildren.oa.utils.PassUtils
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import com.fqchildren.oa.utils.docs.ApiType
import com.fqchildren.oa.utils.permission.Permission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@ApiClass(value = "用户管理")
@RestController
@RequestMapping(value = ["/user"], method = [RequestMethod.POST])
class UserRest {

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    @Autowired
    private lateinit var userService: UserService

    @Permission
    @ApiMethod(value = "解析密码", depict = "将用户的密文密码解密",
            params = [
                ApiParam(value = "password", depict = "密文密码")
            ])
    @RequestMapping(value = ["/resolve.sel"])
    fun resolve(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("password")) {
            return params.fail()
        }
        val password = params.string("password")
        val resolve = PassUtils.decryptAes(password)
        return RestModel(data = resolve)
    }

    @Permission
    @ApiMethod(value = "用户总数", depict = "获取用户列表的用户总数")
    @RequestMapping(value = ["/count.sel"])
    fun count(@RequestBody params: ParamModel): RestModel {
        return RestModel(data = userInfoMapper.count(UserInfo().sql()))
    }

    @Permission
    @ApiMethod(value = "用户列表", depict = "获取系统中的所有用户，分页列表",
            params = [
                ApiParam(value = "search", depict = "搜索关键字", required = false),
                ApiParam(value = "st", depict = "毫秒时间范围，开始", type = ApiType.Long),
                ApiParam(value = "et", depict = "毫秒时间范围，结束", type = ApiType.Long),
                ApiParam(value = "page", depict = "页数，从0开始", type = ApiType.Int),
                ApiParam(value = "limit", depict = "大小，每页大小", type = ApiType.Int)
            ])
    @RequestMapping(value = ["/user.sel"])
    fun select(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("page", "limit")) {
            return params.fail()
        }
        val search = params.sqlSearch()
        val st = params.sqlSt()
        val et = params.sqlEt()
        val page = params.sqlPage()
        val limit = params.sqlLimit()

        val userList = userInfoMapper.selectUserList(st, et, page, limit, search)
        userList.forEach { user ->
            if (user.containsKey("password")) {
                val password = user.getOrDefault("password", "").toString()
                if (password.isNotEmpty()) {
                    user["password"] = PassUtils.decryptAes(password).toString()    // 解析密码
                }
            }
            if (user.containsKey("uid")) {
                val uid = user.getOrDefault("uid", "").toString()
                if (uid.isNotEmpty()) {
                    user["roleList"] = userRoleMapper.selectUserRoles(uid)  // 读取用户角色
                }
            } else {
                user["roleList"] = setOf<Any>()
            }
        }

        return RestModel(data = userList)
    }

    @Permission
    @ApiMethod(value = "添加用户",
            depict = "添加一个用户，必须指定电话号码",
            params = [
                ApiParam(value = "phone", depict = "电话"),
                ApiParam(value = "email", depict = "邮箱", required = false),
                ApiParam(value = "wxOid", depict = "微信OpenId", required = false),
                ApiParam(value = "username", depict = "名称"),
                ApiParam(value = "password", depict = "密码(6-12位)"),
                ApiParam(value = "photo", depict = "头像URL", required = false),
                ApiParam(value = "gender", depict = "性别", required = false),
                ApiParam(value = "regionId", depict = "地区ID", required = false),
                ApiParam(value = "depId", depict = "部门ID", required = false)
            ])
    @RequestMapping(value = ["/user.ins"])
    fun insert(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("phone", "username", "password")) {
            return params.fail()
        }
        val phone = params.string("phone")
        val email = params.string("email")
        val wxOid = params.string("wxOid")
        val username = params.string("username")
        val password = params.string("password")
        val photo = params.string("photo")
        val gender = params.int("gender")
        val regionId = params.string("regionId")
        val depId = params.string("depId")

        val sqlUserInfo = userInfoMapper.select(UserInfo(phone = phone).sql())
        if (sqlUserInfo != null) {
            return RestModel(code = -1, msg = "电话号码已经存在")
        }
        val userInfo = UserInfo()
        userInfo.phone = phone
        userInfo.email = email
        userInfo.wxOid = wxOid
        userInfo.username = username
        userInfo.password = PassUtils.encryptAes(password)
        userInfo.photo = photo
        userInfo.gender = gender
        userInfo.regionId = regionId
        userInfo.depId = depId
        userInfoMapper.insert(userInfo)

        return RestModel(data = userInfo)
    }

    @Permission
    @ApiMethod(value = "修改用户",
            depict = "修改一个用户的信息，注意：不需要修改的字段需要原样返回",
            params = [
                ApiParam(value = "uid", depict = "用户ID"),
                ApiParam(value = "phone", depict = "电话"),
                ApiParam(value = "email", depict = "邮箱", required = false),
                ApiParam(value = "wxOid", depict = "微信OpenId", required = false),
                ApiParam(value = "username", depict = "名称"),
                ApiParam(value = "password", depict = "密码(6-12位)"),
                ApiParam(value = "photo", depict = "头像URL", required = false),
                ApiParam(value = "gender", depict = "性别", required = false),
                ApiParam(value = "regionId", depict = "地区ID", required = false),
                ApiParam(value = "depId", depict = "部门ID", required = false)
            ])
    @RequestMapping(value = ["/user.upd"])
    fun update(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid", "phone", "username", "password")) {
            return params.fail()
        }
        val uid = params.string("uid")
        val userInfo = userInfoMapper.select(UserInfo().sql(uid = uid))
                ?: return RestModel(code = -1, msg = "用户不存在，修改失败")

        userInfo.phone = params.string("phone")
        userInfo.email = params.string("email")
        userInfo.wxOid = params.string("wxOid")
        userInfo.username = params.string("username")
        userInfo.password = PassUtils.encryptAes(params.string("password"))
        userInfo.photo = params.string("photo")
        userInfo.gender = params.int("gender")
        userInfo.regionId = params.string("regionId")
        userInfo.depId = params.string("depId")
        userInfoMapper.update(userInfo)

        return RestModel(data = userInfo)
    }

    @Permission
    @ApiMethod(value = "删除用户",
            depict = "删除一个用户，同时相关的用户角色等数据也会被删除,返回受影响的数据统计",
            params = [
                ApiParam(value = "uid", depict = "用户ID")
            ])
    @RequestMapping(value = ["/user.del"])
    fun delete(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("uid")) {
            return params.fail()
        }
        val uid = params.string("uid")
        return RestModel(data = userService.deleteUser(uid))
    }

    @Permission
    @ApiMethod(value = "用户角色",
            depict = "查询用户角色列表",
            params = [
                ApiParam(value = "userId", depict = "用户ID")
            ])
    @RequestMapping(value = ["/role.sel"])
    fun roleSel(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("userId")) {
            return params.fail()
        }
        val userId = params.string("userId")
        return RestModel(data = userRoleMapper.list(UserRole(userId = userId).sql()))
    }

    @Permission
    @ApiMethod(value = "绑定角色",
            depict = "为用户绑定角色，一个用户可以绑定多个角色",
            params = [
                ApiParam(value = "userId", depict = "用户ID"),
                ApiParam(value = "roleIds",
                        depict = "角色ID列表，多个用 & 隔开，空字符串代表清空",
                        example = "v1&v2&v3")
            ])
    @RequestMapping(value = ["/role.upd"])
    fun roleUpd(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("userId")) {
            return params.fail()
        }
        val userId = params.string("userId")
        val roleIds = params.string("roleIds")
        return RestModel(data = userService.bindingRoles(userId, roleIds))
    }

    @Permission
    @ApiMethod(value = "用户信息", depict = "根绝电话UID/电话查询用户信息,两个参数只能传递一个",
            params = [
                ApiParam(value = "uid", depict = "用户ID", required = false),
                ApiParam(value = "phone", depict = "用户ID", required = false)
            ])
    @RequestMapping(value = ["/info.sel"])
    fun info(@RequestBody params: ParamModel): RestModel {
        val uid = params.string("uid")
        val userInfo = (if (uid.isNotEmpty()) {
            userInfoMapper.select(UserInfo().sql(uid = uid))
        } else {
            val phone = params.string("phone")
            if (phone.isEmpty()) {
                return RestModel(code = -1, msg = "参数错误")
            }
            userInfoMapper.select(UserInfo(phone = phone).sql())
        }) ?: return RestModel(code = -2, msg = "用户不存在")
        return RestModel(data = userInfo)
    }

}
