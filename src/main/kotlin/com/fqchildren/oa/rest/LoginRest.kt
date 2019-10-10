package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RequestModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.service.LoginService
import com.fqchildren.oa.table.PermissionApiMapper
import com.fqchildren.oa.table.UserInfo
import com.fqchildren.oa.table.UserInfoMapper
import com.fqchildren.oa.utils.PassUtils
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@ApiClass(value = "登陆管理")
@RestController
@RequestMapping(value = ["/login"], method = [RequestMethod.POST])
class LoginRest : BaseRest {

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var permissionApiMapper: PermissionApiMapper

    @Autowired
    private lateinit var loginService: LoginService

    @Autowired
    private lateinit var smsRest: SmsRest

    @ApiMethod(value = "电话是否存在",
            depict = "获取短信验证码时，检查系统中是否已经有该号码存在",
            params = [
                ApiParam(value = "phone", depict = "电话号码")
            ])
    @RequestMapping(value = ["/exist.sel"])
    fun exist(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("phone")) {
            return params.fail()
        }
        val phone = params.string("phone")

        val userInfo = userInfoMapper.select(UserInfo(phone = phone).sql())

        return RestModel(code = 0, msg = "修改完成", data = userInfo != null)
    }

    @ApiMethod(value = "账号登陆",
            depict = "用户用电话或者邮箱登陆系统",
            params = [
                ApiParam(value = "username", depict = "登陆名（电话、邮箱）"),
                ApiParam(value = "password", depict = "登陆密码")
            ],
            result = [
                ApiParam(value = "token", depict = "token字符串")
            ])
    @RequestMapping(value = ["/account.do"])
    fun account(@RequestBody params: ParamModel, request: HttpServletRequest): RestModel {
        if (!params.verify("username", "password")) {
            return params.fail()
        }
        val username = params.string("username")
        val password = params.string("password")

        val userInfo = when {
            loginService.accountIsPhone(username) -> userInfoMapper.select(UserInfo(phone = username).sql())
            loginService.accountIsEmail(username) -> userInfoMapper.select(UserInfo(email = username).sql())
            else -> null
        } ?: return RestModel(code = -1, msg = "账号不存在")

        val aesPass = PassUtils.encryptAes(password)
        if (userInfo.password != aesPass) {
            return RestModel(code = -2, msg = "密码错误，或者账号错误")
        }

        val clientId = if (request is RequestModel) request.header.id else ""
        val data = loginService.createLoginData(userInfo, clientId)

        return RestModel(code = 0, msg = "登录成功", data = data)
    }

    @ApiMethod(value = "微信登陆",
            depict = "用微信公众号的OpenId登陆",
            params = [
                ApiParam(value = "openId", depict = "微信公众用户ID")
            ],
            result = [
                ApiParam(value = "token", depict = "token字符串")
            ])
    @RequestMapping(value = ["/wxGzhId.do"])
    fun wxGzhId(@RequestBody params: ParamModel, request: HttpServletRequest): RestModel {
        if (!params.verify("openId")) {
            return params.fail()
        }
        val openId = params.string("openId")
        val userInfo = userInfoMapper.select(UserInfo(wxOid = openId).sql())
                ?: return RestModel(code = -1, msg = "信息不存在，请先绑定")

        val clientId = if (request is RequestModel) request.header.id else ""
        val data = loginService.createLoginData(userInfo, clientId)

        return RestModel(code = 0, msg = "登录成功", data = data)
    }

    @ApiMethod(value = "电话注册", depict = "用电话号码注册，需要短信验证码",
            params = [
                ApiParam(value = "phone", depict = "电话号码"),
                ApiParam(value = "code", depict = "短信验证码"),
                ApiParam(value = "password", depict = "登陆密码")
            ])
    @RequestMapping(value = ["/register.do"])
    fun register(@RequestBody params: ParamModel, request: HttpServletRequest): RestModel {
        if (!params.verify("phone", "code", "password")) {
            return params.fail()
        }
        val phone = params.string("phone")
        val code = params.string("code")
        val password = params.string("password")

        var userInfo = userInfoMapper.select(UserInfo(phone = phone).sql())
        if (userInfo != null) {
            return RestModel(code = -1, msg = "你的手机号码已经注册过")
        }

        if (password.length < 6 || password.length > 12) {
            return RestModel(code = -2, msg = "密码长度需要在6到12位范围")
        }

        if (!smsRest.verifyCode(phone, code)) {
            return smsRest.failCode()
        }

        userInfo = UserInfo()
        userInfo.phone = phone
        userInfo.password = PassUtils.encryptAes(password)
        userInfoMapper.insert(userInfo)

        return RestModel(data = userInfo)
    }

    @ApiMethod(value = "绑定（注册）账号", depict = "微信登陆时绑定用户电话号码，没有则自动创建一个",
            params = [
                ApiParam(value = "phone", depict = "电话号码"),
                ApiParam(value = "code", depict = "短信验证码"),
                ApiParam(value = "openId", depict = "微信ID")
            ])
    @RequestMapping(value = ["/binding.do"])
    fun binding(@RequestBody params: ParamModel, request: HttpServletRequest): RestModel {
        if (!params.verify("phone", "code", "openId")) {
            return params.fail()
        }
        val phone = params.string("phone")
        val code = params.string("code")
        val openId = params.string("openId")

        var userInfo = userInfoMapper.select(UserInfo(wxOid = openId).sql())
        if (userInfo != null) {
            return RestModel(code = -1, msg = "你的微信已经绑定过账号${userInfo.phone}，请直接登陆")
        }

        if (!smsRest.verifyCode(phone, code)) {
            return smsRest.failCode()
        }

        userInfo = userInfoMapper.select(UserInfo(phone = phone).sql())
        if (userInfo == null) {
            userInfo = UserInfo()
            userInfo.phone = phone
            userInfo.wxOid = openId
            userInfoMapper.insert(userInfo)
        } else {
            /*if (userInfo.wxOid.isNotEmpty() || userInfo.wxOid != openId) {
                return RestModel(code = -2, msg = "你的电话号码已经绑定过微信${userInfo.phone}，请先解绑")
            }*/
            userInfo.wxOid = openId
            userInfoMapper.update(userInfo)
        }

        return RestModel(data = userInfo)
    }

    @ApiMethod(value = "修改（重置）密码",
            depict = "默认微信登陆时没有密码的，不需要旧密码，安全性通过电话号码与微信保证",
            params = [
                ApiParam(value = "password", depict = "新密码")
            ])
    @RequestMapping(value = ["/password.upd"])
    fun password(@RequestBody params: ParamModel, request: RequestModel): RestModel {
        if (!params.verify("password")) {
            return params.fail()
        }
        val userId = request.token.uid
        val password = params.string("password")

        val userInfo = userInfoMapper.select(UserInfo().sql(uid = userId))
                ?: return RestModel(code = -1, msg = "用户不存在")

        userInfo.password = PassUtils.encryptAes(password)

        userInfoMapper.update(userInfo)

        return RestModel(code = 0, msg = "修改完成")
    }

    @ApiMethod(value = "用户权限接口", depict = "获取用户拥有的权限接口列表,通过角色映射绑定的接口",
            params = [
                ApiParam(value = "userId", depict = "用户ID")
            ])
    @RequestMapping(value = ["/permissionApis.sel"])
    fun permissionApis(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("userId")) {
            return params.fail()
        }
        val apis = permissionApiMapper.selectUserApis(params.string("userId"))
        return RestModel(code = 0, msg = "获取完成", data = apis)
    }

    @ApiMethod(value = "刷新用户信息",
            depict = "重新刷新用户的登录信息数据,返回数据与登录接口一致,userId不传默认使用当前登录用户",
            params = [
                ApiParam(value = "userId", depict = "用户ID", required = false)
            ])
    @RequestMapping(value = ["/refresh.do"])
    fun refresh(@RequestBody params: ParamModel, request: HttpServletRequest): RestModel {
        var userId = params.string("userId")
        if (userId.isEmpty() && request is RequestModel) {
            userId = request.token.uid
        }
        if (userId.isEmpty()) {
            return RestModel(code = -999, msg = "用户ID为空")
        }
        val clientId = if (request is RequestModel) request.header.id else ""
        val data = loginService.createLoginData(userId, clientId)
        return RestModel(data = data)
    }

}
