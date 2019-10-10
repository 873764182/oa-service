package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Component

@Mapper
@Component
interface DepartmentAssistantMapper : BaseMapper<DepartmentAssistant> {
}
