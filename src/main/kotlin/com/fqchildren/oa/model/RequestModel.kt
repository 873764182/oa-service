package com.fqchildren.oa.model

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/**
 * 包装默认的请求对象，同时解析参数
 */
class RequestModel constructor(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    // private val log = LoggerFactory.getLogger(RequestModel::class.java)

    private var requestData: ByteArray? = null

    var header: HeaderModel = HeaderModel()

    var token: TokenModel = TokenModel.createToken("")

    init {
        requestData = request.inputStream.readBytes()

        this.initRequestData(request)
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream))
    }

    override fun getInputStream(): ServletInputStream {
        val bais = ByteArrayInputStream(requestData)
        return object : ServletInputStream() {
            override fun isReady(): Boolean {
                return true
            }

            override fun isFinished(): Boolean {
                return true
            }

            override fun setReadListener(p0: ReadListener?) {
            }

            override fun read(): Int {
                return bais.read()
            }
        }
    }

    private fun initRequestData(req: HttpServletRequest) {
        fun getHeader(request: HttpServletRequest, name: String, defValue: String = ""): String {
            val value = request.getHeader(name)
            if (value.isNullOrEmpty()) {
                return defValue
            }
            return value
        }
        header.ip = getHeader(req, "X-Real-IP")   // 从 NGINX 传入
        header.time = getHeader(req, "h-time", "0").toLong()
        header.id = getHeader(req, "h-id")
        header.type = getHeader(req, "h-type")
        header.ver = getHeader(req, "h-ver", "0").toInt()
        header.token = getHeader(req, "h-token")

        if (header.token.isNotEmpty()) {
            val t = TokenModel.decryptToken(header.token)
            if (t != null) {
                token = t
            }
        }
    }

}
