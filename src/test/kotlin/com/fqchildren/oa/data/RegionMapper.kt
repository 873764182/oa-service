package com.fqchildren.oa.data

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface RegionMapper {

    @Select(value = ["SELECT * FROM region WHERE 1=1"])
    fun selectOldData(): Set<OldModel>

}
