package com.fqchildren.oa.service

import com.fqchildren.oa.table.PermissionApi
import com.fqchildren.oa.table.PermissionApiMapper
import com.fqchildren.oa.table.PermissionInfo
import com.fqchildren.oa.table.PermissionInfoMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URLDecoder

@Service
class PerService {

    @Autowired
    private lateinit var permissionApiMapper: PermissionApiMapper

    @Autowired
    private lateinit var permissionInfoMapper: PermissionInfoMapper

    /**
     * 删除权限，同时删除相关的API引用
     */
    @Transactional
    fun deletePermission(perId: String): Map<String, Int> {
        val apiCount = permissionApiMapper.delete(PermissionApi(perId = perId).sql())
        val infoCount = permissionInfoMapper.delete(PermissionInfo().sql(uid = perId))
        return mapOf("apiCount" to apiCount, "infoCount" to infoCount)
    }

    /**
     * 为权限绑定相应的APIS接口
     * 1.删除目前绑定的接口
     * 2.重新赋值绑定新接口
     */
    @Transactional
    fun bindingApis(perId: String, apiData: String): Map<String, Int> {
        fun decoder(data: String): String {
            return URLDecoder.decode(data, "UTF-8")
        }

        fun resolve(data: String): PermissionApi {  // 需要注意顺序与关键字，内容中不能出现“|”与“&”等关键字
            val arr = data.split("|")
            val permissionApi = PermissionApi()
            permissionApi.perId = perId
            permissionApi.name = decoder(arr[0])
            permissionApi.depict = decoder(arr[1])
            permissionApi.api = decoder(arr[2])
            return permissionApi
        }

        val deleteCount = permissionApiMapper.delete(PermissionApi(perId = perId).sql())
        var insertCount = 0
        if (apiData.isEmpty()) {
            return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
        }
        if (!apiData.contains("&")) {
            val permissionApi = resolve(apiData)
            permissionApi.perId = perId
            insertCount = permissionApiMapper.insert(permissionApi)
            return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
        }
        apiData.split("&").forEach {
            val permissionApi = resolve(it)
            permissionApi.perId = perId
            insertCount += permissionApiMapper.insert(permissionApi)
        }
        return mapOf("deleteCount" to deleteCount, "insertCount" to insertCount)
    }

}
