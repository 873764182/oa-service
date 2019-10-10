package com.fqchildren.oa.service

import com.fqchildren.oa.rest.AppRest
import com.fqchildren.oa.table.*
import com.fqchildren.oa.utils.AsyncInterface
import com.fqchildren.oa.utils.FunUtils
import com.fqchildren.oa.utils.ThreadUtils
import com.fqchildren.oa.utils.permission.PerUtils
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.stereotype.Service

@Service
class InitService {
    private val log = LoggerFactory.getLogger(InitService::class.java)

    private val uid = "100000"

    @Autowired
    private lateinit var regionInfoMapper: RegionInfoMapper

    @Autowired
    private lateinit var roleInfoMapper: RoleInfoMapper

    @Autowired
    private lateinit var permissionInfoMapper: PermissionInfoMapper

    @Autowired
    private lateinit var rolePermissionMapper: RolePermissionMapper

    @Autowired
    private lateinit var permissionApiMapper: PermissionApiMapper

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    @Autowired
    private lateinit var departmentInfoMapper: DepartmentInfoMapper

    /**
     * 初始化系统需要的基础数据环境
     */
    fun run(event: ApplicationReadyEvent?) {
        ThreadUtils.delayMethod(object : AsyncInterface {
            override fun run() {
                checkRegion()
                checkRole()
                checkPermission()
                checkApis()
                checkAdminUser()
                checkDepartment()
                checkDeleteInvalidApis()
            }
        }, 2000)
    }

    /**
     * 检查地区数据是否正确，缺少的将自动按源数据补充
     */
    private fun checkRegion() {
        val sqlIds = regionInfoMapper.selectAllIds()
        val regionData = FunUtils.getClassPathFile("/data/sql/region-data.json")
        val jsonList = JSONArray(regionData)
        for (it in jsonList) {
            if (it == null) {
                continue
            }
            try {
                val region = it as JSONObject
                val uid = region.getString("uid")
                if (!uid.isNullOrEmpty() && uid != "null") {
                    if (!sqlIds.contains(uid)) {
                        val ri = RegionInfo()
                        ri.uid = uid
                        ri.time = 0
                        ri.pid = region.getString("pid")
                        ri.name = region.getString("name")
                        ri.shortName = region.getString("shortName")
                        ri.fullName = region.getString("fullName")
                        ri.englishName = region.getString("englishName")
                        ri.levelType = region.getInt("levelType")
                        ri.cityCode = region.getString("cityCode")
                        ri.zipCode = region.getString("zipCode")
                        ri.longitude = region.getFloat("longitude")
                        ri.latitude = region.getFloat("latitude")
                        regionInfoMapper.insert(ri)
                    }
                }
            } catch (e: Exception) {
                log.error("checkRegion：${e.message}")
            }
        }
    }

    /**
     * 检查角色数据（插入超级管理员角色）
     */
    private fun checkRole() {
        val roleData = FunUtils.getClassPathFile("/data/sql/role-data.json")
        val jsonList = JSONArray(roleData)
        for (it in jsonList) {
            if (it == null) {
                continue
            }
            val role = it as JSONObject
            val uid = role.getString("uid")
            if (!uid.isNullOrEmpty() && uid != "null") {
                var roleInfo = roleInfoMapper.select(RoleInfo().sql(uid = uid))
                if (roleInfo == null) {
                    roleInfo = RoleInfo()
                    roleInfo.uid = uid
                    roleInfo.time = role.getLong("time")
                    roleInfo.name = role.getString("name")
                    roleInfo.depict = role.getString("depict")
                    roleInfoMapper.insert(roleInfo)
                }
            }
        }
    }

    /**
     * 检查权限信息（为超级管理员添加一个配套权限）
     */
    private fun checkPermission() {
        val permissionData = FunUtils.getClassPathFile("/data/sql/permission-data.json")
        val jsonList = JSONArray(permissionData)
        for (it in jsonList) {
            if (it == null) {
                continue
            }
            val permission = it as JSONObject
            val uid = permission.getString("uid")
            if (!uid.isNullOrEmpty() && uid != "null") {
                var permissionInfo = permissionInfoMapper.select(PermissionInfo().sql(uid = uid))
                if (permissionInfo == null) {
                    permissionInfo = PermissionInfo()
                    permissionInfo.uid = uid
                    permissionInfo.time = permission.getLong("time")
                    permissionInfo.name = permission.getString("name")
                    permissionInfo.depict = permission.getString("depict")
                    permissionInfoMapper.insert(permissionInfo)
                }
            }
        }

        // 检查超级管理员角色是否与配套的权限绑定在一起，uid 固定为 100000 不能变
        var rolePermission = rolePermissionMapper
                .select(RolePermission(roleId = uid, perId = uid).sql())
        if (rolePermission == null) {
            rolePermission = RolePermission()
            rolePermission.roleId = uid
            rolePermission.perId = uid
            rolePermissionMapper.insert(rolePermission)
        }
    }

    /**
     * 检查权限API是否完整（为管理员添加接口访问列表，超级管理需要可以访问所有受限制的接口）
     */
    private fun checkApis() {
        val apiList = permissionApiMapper.selectPerApis(uid)
        val interfaceList = PerUtils.getApiData(AppRest.apis_pack_name)
        for (map in interfaceList) {
            val method = map.getOrDefault("apiList", null)
            if (method != null) {
                for (data in (method as Set<*>)) {
                    val mapData = (data as Map<*, *>)
                    val apis = mapData.getOrDefault("apis", "").toString()
                    if (apis.isNotEmpty()) {
                        if (!apiList.contains(apis)) {
                            val permissionApi = PermissionApi()
                            permissionApi.api = apis
                            permissionApi.name = mapData.getOrDefault("name", "").toString()
                            permissionApi.depict = mapData.getOrDefault("depict", "").toString()
                            permissionApi.perId = uid
                            permissionApiMapper.insert(permissionApi)
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查管理员用户，没有则添加
     */
    private fun checkAdminUser() {
        val adminData = FunUtils.getClassPathFile("/data/sql/admin-data.json")
        val jsonList = JSONArray(adminData)
        for (it in jsonList) {
            if (it == null) {
                continue
            }
            val admin = it as JSONObject
            val uid = admin.getString("uid")
            if (!uid.isNullOrEmpty() && uid != "null") {
                var adminInfo = userInfoMapper.select(UserInfo().sql(uid = uid))
                if (adminInfo == null) {
                    adminInfo = UserInfo()
                    adminInfo.uid = uid
                    adminInfo.time = admin.getLong("time")
                    adminInfo.phone = admin.getString("phone")
                    adminInfo.email = admin.getString("email")
                    adminInfo.wxOid = admin.getString("wxOid")
                    adminInfo.username = admin.getString("username")
                    adminInfo.password = admin.getString("password")
                    adminInfo.photo = admin.getString("photo")
                    adminInfo.gender = admin.getInt("gender")
                    adminInfo.regionId = admin.getString("regionId")
                    adminInfo.depId = admin.getString("depId")
                    userInfoMapper.insert(adminInfo)
                }
            }
        }

        // 给管理员账号绑定管理员角色
        var userRole = userRoleMapper.select(UserRole(roleId = uid, userId = uid).sql())
        if (userRole == null) {
            userRole = UserRole()
            userRole.roleId = uid
            userRole.userId = uid
            userRoleMapper.insert(userRole)
        }
    }

    /**
     * 检查部门信息，部门需要一个节点作为根节点部门
     */
    private fun checkDepartment() {
        val departmentData = FunUtils.getClassPathFile("/data/sql/department-data.json")
        val jsonList = JSONArray(departmentData)
        for (it in jsonList) {
            if (it == null) {
                continue
            }
            val department = it as JSONObject
            val uid = department.getString("uid")
            if (!uid.isNullOrEmpty() && uid != "null") {
                var departmentInfo: DepartmentInfo? = departmentInfoMapper.select(DepartmentInfo().sql(uid = uid))
                if (departmentInfo == null) {
                    departmentInfo = DepartmentInfo()
                    departmentInfo.uid = uid
                    departmentInfo.time = department.getLong("time")
                    departmentInfo.pid = department.getString("pid")
                    departmentInfo.name = department.getString("name")
                    departmentInfo.code = department.getString("code")
                    departmentInfo.adminUser = department.getString("adminUser")
                    departmentInfo.regionId = department.getString("regionId")
                    departmentInfoMapper.insert(departmentInfo)
                }
            }
        }
    }

    /**
     * 随着系统的更新，有些接口会被移除，要更新到权限接口，删除无用的接口映射
     */
    private fun checkDeleteInvalidApis() {
        // 获取到目前系统有效的接口列表
        val interfaceList = PerUtils.getApiData(AppRest.apis_pack_name)
        val validApis = mutableSetOf<String>()
        for (map in interfaceList) {
            val method = map.getOrDefault("apiList", null)
            if (method != null) {
                for (data in (method as Set<*>)) {
                    val mapData = (data as Map<*, *>)
                    val apis = mapData.getOrDefault("apis", "").toString()
                    if (apis.isNotEmpty()) {
                        validApis.add(apis)
                    }
                }
            }
        }

        // 查询目前已经被系统引用的接口列表
        val apiList = permissionApiMapper.selectAllApis()
        apiList.forEach {
            if (!validApis.contains(it)) {
                permissionApiMapper.deleteByApi(it.trim())  // 已经失效的接口则直接删除引用
            }
        }
    }

}
