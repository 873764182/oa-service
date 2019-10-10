package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface DepartmentInfoMapper : BaseMapper<DepartmentInfo> {

    /**
     * 获取所有部门 带管理员名称与地区名称
     */
    @Select(value = ["SELECT " +
            "  di.uid, " +
            "  di.time, " +
            "  di.pid, " +
            "  di.NAME, " +
            "  di.CODE, " +
            "  di.adminUser, " +
            "  di.regionId, " +
            "  ui.username AS adminName, " +
            "  ri.fullName AS regionName " +
            "FROM " +
            "  department_info AS di " +
            "LEFT JOIN user_info AS ui ON ui.uid = di.adminUser " +
            "LEFT JOIN region_info AS ri ON ri.uid = di.regionId "])
    fun selectAllTree(): Set<Map<String, Any>>

}
