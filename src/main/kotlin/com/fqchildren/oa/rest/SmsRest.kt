package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RequestModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.table.SmsInfo
import com.fqchildren.oa.table.SmsInfoMapper
import com.fqchildren.oa.utils.PassUtils
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import sun.misc.BASE64Encoder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

@ApiClass(value = "短信管理")
@RestController
@RequestMapping(value = ["/sms"], method = [RequestMethod.POST])
class SmsRest : BaseRest {

    @Autowired
    private lateinit var smsInfoMapper: SmsInfoMapper

    @ApiMethod(value = "发送短信验证码", depict = "注册或者绑定手机号码时需要验证码验证", params = [
        ApiParam(value = "phone", depict = "目标电话号码")
    ])
    @RequestMapping(value = ["/code.do"])
    fun code(@RequestBody params: ParamModel, requestModel: RequestModel): RestModel {
        val phone = params.string("phone")
        if (phone.isEmpty() || phone.length != 11) {
            return RestModel(code = -1, msg = "电话号码必须为11位长度")
        }
        val ct = System.currentTimeMillis()
        val cIp = requestModel.header.ip
        val sis = smsInfoMapper.selectListByIp(ct - (10 * 60 * 1000), ct, cIp)
        if (sis.size >= 5) {  // 同一个网络环境下10分钟内发送的短信条数不能超过5条
            return RestModel(code = -2, msg = "检查到该环境存在安全问题，请稍后再试")
        }
        var smsInfo = smsInfoMapper.selectLatestData(phone)
        if (smsInfo != null && smsInfo.time > (ct - (60 * 1000))) {
            return RestModel(code = -3, msg = "发送过于频繁，请一分钟后再次发送")
        }
        smsInfo = SmsInfo()
        smsInfo.phone = phone
        smsInfo.value = createCode()
        smsInfo.cIp = requestModel.header.ip
        smsInfo.cId = requestModel.header.id

        val result = doSendSms(smsInfo.phone, smsInfo.value)
        if (!result) {
            return RestModel(code = -4, msg = "验证码发送失败，请查看服务日志")
        }
        smsInfoMapper.insert(smsInfo)

        return RestModel(code = 0, msg = "发送成功")
    }

    /**
     * 检查验证码是否正确
     *
     * 获取指定时间范围内的短信验证码，只要有一个匹配则认为验证码正确
     */
    fun verifyCode(phone: String, code: String): Boolean {
        val ct = System.currentTimeMillis()
        val codeList = smsInfoMapper.selectValueByTime(ct - (verify_time * 60 * 1000), ct, phone)
        return codeList.contains(code.trim())
    }

    /**
     * 验证码错误时，返回一个通用的提示信息
     *
     * 获取指定时间范围内的短信验证码，只要有一个匹配则认为验证码正确
     */
    fun failCode(): RestModel {
        return RestModel(code = -555, msg = "短信验证码错误，请稍等一会后重新获取试试")
    }

    /**
     * 短信发送基于 容联云 平台
     *
     * http://www.yuntongxun.com/doc/rest/sms/3_2_1_1.html
     */
    companion object {
        private val log = LoggerFactory.getLogger(SmsRest::class.java)

        // TODO 请前往容联云申请相应的短信秘钥
        private const val account_sid = ""
        private const val auth_token = ""
        private const val app_id = ""

        private const val url_prefix = "https://app.cloopen.com:8883/2013-12-26/Accounts/"
        private const val url_suffix = "/SMS/TemplateSMS?sig="

        private const val verify_time = 5 // 分钟

        /**
         * 发送短信验证码
         */
        private fun doSendSms(phone: String, code: String): Boolean {
            val dateString = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val sign = PassUtils.md5("$account_sid$auth_token$dateString").toUpperCase()
            val url = "$url_prefix$account_sid$url_suffix$sign"
            val author = BASE64Encoder().encode(
                    "$account_sid:$dateString".toByteArray(Charset.defaultCharset()))
            val json = JSONObject()
            json.put("to", phone)
            json.put("appId", app_id)
            json.put("templateId", 416116)
            json.put("datas", setOf(code, "${verify_time}分钟"))
            val params = json.toString()

            try {
                val restTemplate = RestTemplate()
                val headers = HttpHeaders()
                headers.accept = listOf(MediaType.APPLICATION_JSON_UTF8)
                headers.contentType = MediaType.APPLICATION_JSON_UTF8
                headers.contentLength = params.length.toLong()
                headers.add("authorization", author)
                val entity = HttpEntity(params, headers)
                val result = restTemplate.postForObject(url, entity, String::class.java)
                // {"statusCode":"000000","templateSMS":{"dateCreated":"20130201155306", "smsMessageSid":" ff8080813c373cab013c94b0f0512345"}}
                log.warn(result)
                if (result.isNullOrEmpty()) {
                    return false
                }
                val jsonMap = JSONObject(result)
                val statusCode = jsonMap.getString("statusCode").toString()
                return statusCode == "000000"
            } catch (e: Exception) {
                return false
            }
        }

        /**
         * 获取6位随机码
         */
        private fun createCode(): String {
            val random = Random()
            val num1: Int = random.nextInt(10) // 0 - 9
            val num2: Int = random.nextInt(10) // 0 - 9
            val num3: Int = random.nextInt(10) // 0 - 9
            val num4: Int = random.nextInt(10) // 0 - 9
            val num5: Int = random.nextInt(10) // 0 - 9
            val num6: Int = random.nextInt(10) // 0 - 9
            return "$num1$num2$num3$num4$num5$num6"
        }
    }

}
