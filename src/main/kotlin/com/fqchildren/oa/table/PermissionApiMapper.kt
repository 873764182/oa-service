package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface PermissionApiMapper : BaseMapper<PermissionApi> {

    /**
     * 1. 通过用户ID获取用户的角色
     * 2. 通过角色获取用户的权限
     * 3. 通过权限获取用的API列表
     */
    @Select(value = ["SELECT " +
            " api " +
            "FROM " +
            " permission_api " +
            "WHERE " +
            " perId IN ( " +
            "  SELECT perId FROM role_permission " +
            "  WHERE roleId IN ( " +
            "    SELECT roleId FROM user_role WHERE userId = #{userId} " +
            "   ) " +
            " ) "])
    fun selectUserApis(@Param("userId") userId: String): Set<String>

    @Select(value = ["SELECT api FROM permission_api WHERE perId = #{perId}"])
    fun selectPerApis(@Param("perId") perId: String): Set<String>

    @Select(value = ["SELECT api FROM permission_api GROUP BY api"])
    fun selectAllApis(): Set<String>

    @Delete(value = ["DELETE FROM permission_api WHERE api = #{api}"])
    fun deleteByApi(@Param("api") api: String): Long = 0

}
