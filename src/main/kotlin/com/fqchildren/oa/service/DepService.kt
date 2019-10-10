package com.fqchildren.oa.service

import com.fqchildren.oa.table.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DepService {

    @Autowired
    private lateinit var departmentInfoMapper: DepartmentInfoMapper

    @Autowired
    private lateinit var departmentAssistantMapper: DepartmentAssistantMapper

    @Autowired
    private lateinit var userInfoMapper: UserInfoMapper

    @Autowired
    private lateinit var regionInfoMapper: RegionInfoMapper

    /**
     * 检查用户是否有操作部门的权限
     * 1. 自己是当前部门的管理员
     * 2. 自己是当前部门的上上级部门管理员，只要自己这个部门的任何上级部门的管理员都可以操作
     * 注意：部门助理只能修改当前部门，没有层级递归权限
     */
    fun checkUserPermission(userId: String, depId: String): Boolean {
        val isAdmin = userIsAdmin(userId, depId)
        val isAssistant = userIsAssistant(userId, depId, manUser = true, manDep = true)
        return isAdmin || isAssistant
    }

    /**
     * 用户是否是管理员，或者上级部门的管理员，逐级递归
     */
    fun userIsAdmin(userId: String, depId: String): Boolean {
        val departmentInfo = departmentInfoMapper
                .select(DepartmentInfo().sql(uid = depId)) ?: return false
        return if (departmentInfo.adminUser == userId) {
            true
        } else if (departmentInfo.pid.isEmpty() || departmentInfo.pid == "0") {
            false
        } else {
            userIsAdmin(userId, departmentInfo.pid) // 递归调用
        }
    }

    /**
     * 用户是否是部门助理，或者上级部门的助理，逐级递归
     * manUser = true ：是助理的同时，必须能管理部门的用户
     * manDep = true ：是助理的同时，必须能管理部门的子部门
     */
    fun userIsAssistant(userId: String, depId: String, manUser: Boolean = false, manDep: Boolean = false): Boolean {
        fun isAssistant(uid: String, did: String, mu: Boolean, md: Boolean): Boolean {
            val departmentAssistant = departmentAssistantMapper
                    .select(DepartmentAssistant(depId = did, userId = uid).sql()) ?: return false
            return if (mu && md) {
                (departmentAssistant.manDep > 0 && departmentAssistant.manUser > 0)
            } else if (mu) {
                departmentAssistant.manUser > 0
            } else if (md) {
                departmentAssistant.manDep > 0
            } else {    // (!manUser && !manDep)
                true
            }
        }

        val departmentInfo = departmentInfoMapper
                .select(DepartmentInfo().sql(uid = depId)) ?: return false
        return if (isAssistant(userId, depId, manUser, manDep)) {
            true
        } else if (departmentInfo.pid.isEmpty() || departmentInfo.pid == "0") {
            false
        } else {
            userIsAdmin(userId, departmentInfo.pid) // 递归调用
        }
    }

    /**
     * 获取部门信息
     */
    fun depInfo(dep: DepartmentInfo): Map<String, Any> {
        val data = LinkedHashMap<String, Any>()
        data["uid"] = dep.uid
        data["pid"] = dep.pid
        data["time"] = dep.time
        data["name"] = dep.name
        data["code"] = dep.code

        // 获取部门管理员信息
        if (dep.adminUser.isNotEmpty()) {
            val userInfo = userInfoMapper.select(UserInfo().sql(uid = dep.adminUser))
            if (userInfo != null) {
                data["adminUser"] = mapOf(
                        "userId" to userInfo.uid,
                        "name" to userInfo.username,
                        "phone" to userInfo.phone,
                        "email" to userInfo.email,
                        "photo" to userInfo.photo,
                        "gender" to userInfo.gender
                )
            }
        }

        // 获取部门所在地信息
        if (dep.regionId.isNotEmpty()) {
            val regionInfo = regionInfoMapper.select(RegionInfo().sql(uid = dep.regionId))
            if (regionInfo != null) {
                data["regionInfo"] = mapOf(
                        "regionId" to regionInfo.uid,
                        "name" to regionInfo.name,
                        "fullName" to regionInfo.fullName,
                        "englishName" to regionInfo.englishName
                )
            }
        }

        return data
    }

    /**
     * 获取部门信息
     */
    fun depInfo(depId: String): Map<String, Any>? {
        val dep = departmentInfoMapper
                .select(DepartmentInfo().sql(uid = depId)) ?: return null
        return depInfo(dep)
    }

    /**
     * 查询子部门列表，递归逐层查找
     */
    fun selectSubDepartmentList(uid: String): Set<DepartmentInfo> {
        val departmentInfoList = mutableSetOf<DepartmentInfo>()
        fun initSubDepartments(id: String) {
            val depList =
                    departmentInfoMapper.list(DepartmentInfo(pid = id).sql())
            if (depList.isNotEmpty()) {
                departmentInfoList.addAll(depList)
                depList.forEach {
                    initSubDepartments(it.uid)
                }
            }
        }
        initSubDepartments(uid)
        return departmentInfoList
    }

    /**
     * 删除部门，同时相关的子部门会被删除，部门用户都会变成无部门的用户
     *
     * 返回受影响的 用户数量、被删除的子部门数量
     */
    @Transactional
    fun deleteDepartment(depId: String): Map<String, Long> {
        val depIds = mutableSetOf<String>()
        depIds.add(depId)

        val subDepartmentList = selectSubDepartmentList(depId)
        if (subDepartmentList.isNotEmpty()) {
            subDepartmentList.forEach {
                depIds.add(it.uid)
            }
        }

        var updateUser = 0L
        var deleteDep = 0L
        depIds.forEach {
            updateUser += userInfoMapper.updateUserDepartment(it)   // 清空相关的用户的部门信息，用户变为无部门状态
            deleteDep += departmentInfoMapper.delete(DepartmentInfo().sql(uid = it))    // 删除部门
        }

        return mapOf("updateUser" to updateUser, "deleteDep" to deleteDep)
    }

}
