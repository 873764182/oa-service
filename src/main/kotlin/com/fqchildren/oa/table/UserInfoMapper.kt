package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Component

@Mapper
@Component
interface UserInfoMapper : BaseMapper<UserInfo> {

    /**
     * 清空指定部门用户的部门
     */
    @Update(value = ["UPDATE user_info SET depId = #{newDepId} WHERE depId = #{depId}"])
    fun updateUserDepartment(@Param("depId") depId: String, @Param("newDepId") newDepId: String = ""): Long = 0

    @Select(value = ["<script> SELECT " +
            "   ui.uid, " +
            "   ui.time, " +
            "   ui.phone, " +
            "   ui.email, " +
            "   ui.wxOid, " +
            "   ui.wxAid, " +
            "   ui.username, " +
            "   ui.password, " +
            "   ui.photo, " +
            "   ui.gender, " +
            "   ui.regionId, " +
            "   ui.depId, " +
            "   ri.name AS regionName, " +
            "   ri.fullName AS regionFullName, " +
            "   di.name AS depName " +
            "FROM user_info AS ui " +
            "LEFT JOIN region_info AS ri ON ri.uid = ui.regionId " +
            "LEFT JOIN department_info AS di ON di.uid = ui.depId " +
            "WHERE ( " +
            "   (1 = 1) " +
            "   <when test=\"st > 0 and et > 0\">" +
            "       AND (ui.time BETWEEN #{st} AND #{et}) " +
            "   </when>" +
            "   <when test=\"search.length > 0\">" +
            "       AND (ui.username LIKE '%\${search}%' OR ui.phone LIKE '%\${search}%' OR ui.email LIKE '%\${search}%') " +
            "   </when>" +
            ") ORDER BY ui.time DESC LIMIT #{page}, #{limit} " +
            "</script>"])
    fun selectUserList(
            @Param("st") st: Long,
            @Param("et") et: Long,
            @Param("page") page: Int,
            @Param("limit") limit: Int,
            @Param("search") text: String): LinkedHashSet<MutableMap<String, Any>> = linkedSetOf()

}
