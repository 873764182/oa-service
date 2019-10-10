package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface RegionInfoMapper : BaseMapper<RegionInfo> {

    @Select(value = ["SELECT uid FROM region_info WHERE 1=1"])
    fun selectAllIds(): Set<String>

}
