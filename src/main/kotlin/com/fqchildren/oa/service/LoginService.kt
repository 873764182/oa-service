package com.fqchildren.oa.service

import com.fqchildren.oa.model.TokenModel
import com.fqchildren.oa.table.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LoginService {

    @Autowired
    private lateinit var regionInfoMapper: RegionInfoMapper

    @Autowired
    private lateinit var departmentInfoMapper: DepartmentInfoMapper

    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    @Autowired
    private lateinit var roleInfoMapper: RoleInfoMapper

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    // 账号是电话号码
    fun accountIsPhone(account: String): Boolean {
        return try {
            account.toLong()
            account.length == 11
        } catch (e: Exception) {
            false
        }
    }

    // 账号是电话号码
    fun accountIsEmail(account: String): Boolean {
        return account.contains("@") && account.contains(".")
    }

    // 登录验证通过后生成相应数据
    fun createLoginData(userId: String, clientId: String = ""): Map<String, Any> {
        val userInfo = userInfoMapper.select(UserInfo().sql(uid = userId))
        return if (userInfo == null) {
            mapOf()
        } else {
            createLoginData(userInfo, clientId)
        }
    }

    // 登录验证通过后生成相应数据
    fun createLoginData(userInfo: UserInfo, clientId: String = ""): Map<String, Any> {
        val data = LinkedHashMap<String, Any>()
        data["userId"] = userInfo.uid
        data["regTime"] = userInfo.time
        data["phone"] = userInfo.phone
        data["email"] = userInfo.email
        data["wxOid"] = userInfo.wxOid
        data["username"] = userInfo.username
        data["photo"] = userInfo.photo
        data["gender"] = userInfo.gender

        if (userInfo.regionId.isNotEmpty()) {
            val regionInfo =
                    regionInfoMapper.select(RegionInfo().sql(uid = userInfo.regionId))
            if (regionInfo != null) {
                val region = LinkedHashMap<String, Any>()
                region["uid"] = userInfo.regionId
                region["name"] = regionInfo.name
                data["region"] = region // 添加区域
            }
        }
        if (userInfo.depId.isNotEmpty()) {
            val departmentInfo =
                    departmentInfoMapper.select(DepartmentInfo().sql(uid = userInfo.depId))
            if (departmentInfo != null) {
                val department = LinkedHashMap<String, Any>()
                department["uid"] = userInfo.depId
                department["name"] = departmentInfo.name
                department["adminUser"] = departmentInfo.adminUser
                department["regionId"] = departmentInfo.regionId
                data["department"] = department // 添加部门
            }
        }

        val userRoleList = userRoleMapper.selectUserRoleIds(userId = userInfo.uid)
        val roleInfoList = mutableSetOf<RoleInfo>()
        if (userRoleList.isNotEmpty()) {
            roleInfoList.addAll(roleInfoMapper.inUid(RoleInfo().sql(), userRoleList))
        }
        data["roleList"] = roleInfoList

        val token = TokenModel.createToken(uid = userInfo.uid, did = clientId)
        data["token"] = token.getTokenString() // 添加Token

        return data
    }

}
