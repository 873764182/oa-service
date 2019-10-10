package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface UserRoleMapper : BaseMapper<UserRole> {

    @Select(value = ["SELECT roleId FROM user_role WHERE userId = #{userId}"])
    fun selectUserRoleIds(@Param("userId") userId: String): LinkedHashSet<String> = linkedSetOf()

    @Select(value = ["SELECT " +
            "   ur.roleId, " +
            "   ri.name, " +
            "   ri.depict " +
            "FROM user_role AS ur " +
            "LEFT JOIN role_info AS ri ON ri.uid = ur.roleId " +
            "WHERE " +
            "   ur.userId = #{userId} " +
            "ORDER BY ur.time DESC"])
    fun selectUserRoles(@Param("userId") userId: String): LinkedHashSet<Map<String, Any>> = linkedSetOf()

    @Select(value = ["SELECT " +
            "   ur.uid, " +
            "   ur.roleId, " +
            "   ur.userId, " +
            "   ui.phone, " +
            "   ui.username, " +
            "   ui.photo " +
            "FROM user_role AS ur " +
            "LEFT JOIN user_info AS ui ON ui.uid = ur.userId " +
            "WHERE " +
            "   ur.roleId = #{roleId} " +
            "ORDER BY ur.time DESC"])
    fun selectRoleUsers(@Param("roleId") roleId: String): LinkedHashSet<Map<String, Any>> = linkedSetOf()

}
