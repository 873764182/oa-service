package com.fqchildren.oa.table

import com.fqchildren.oa.base.BaseMapper
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Component

@Mapper
@Component
interface SmsInfoMapper : BaseMapper<SmsInfo> {

    /**
     * 获取最新一条数据
     */
    @Results(value = [
        (Result(property = "uid", column = "uid")),
        (Result(property = "time", column = "time"))
    ])
    @Select(value = ["SELECT * FROM sms_info WHERE phone = #{phone} ORDER BY time DESC LIMIT 0,1"])
    fun selectLatestData(@Param("phone") phone: String): SmsInfo?

    /**
     * 获取一定时间内给指定电话发送的内容集合
     */
    @Select(value = ["SELECT value FROM sms_info WHERE (time BETWEEN #{st} AND #{et}) AND (phone = #{phone})"])
    fun selectValueByTime(@Param("st") st: Long, @Param("et") et: Long, @Param("phone") phone: String): Set<String>

    /**
     * 获取指定时间区间与IP的数据，避免有人刷流量
     */
    @Results(value = [
        (Result(property = "uid", column = "uid")),
        (Result(property = "time", column = "time"))
    ])
    @Select(value = ["SELECT * FROM sms_info WHERE (time BETWEEN #{st} AND #{et}) AND cIp = #{cIp} ORDER BY time DESC"])
    fun selectListByIp(@Param("st") st: Long, @Param("et") et: Long, @Param("cIp") cIp: String): Set<SmsInfo>

}
