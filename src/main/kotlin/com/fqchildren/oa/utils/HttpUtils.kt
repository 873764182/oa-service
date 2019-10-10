package com.fqchildren.oa.utils


import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils
import java.nio.charset.Charset
import javax.net.ssl.HostnameVerifier


/**
 * HttpClient 网络请求
 *
 * https://www.cnblogs.com/yangmengdx3/p/6322380.html
 *
 * https://blog.csdn.net/u011191463/article/details/78664896
 */
object HttpUtils {
    // 设置连接池
    private val mConnectionManager = PoolingHttpClientConnectionManager()
    // 请求配置
    private var mRequestConfig: RequestConfig

    init {
        // 设置连接池大小 最大不要超过1000
        mConnectionManager.maxTotal = 800
        // 比如maxConnTotal =200，maxConnPerRoute =100，那么，如果只有一个路由的话，那么最大连接数也就是100了；如果有两个路由的话，那么它们分别最大的连接数是100，总数不能超过200
        mConnectionManager.defaultMaxPerRoute = 400;
        // 1秒钟不活动后验证连接
        mConnectionManager.validateAfterInactivity = 1000

        val configBuilder = RequestConfig.custom()
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(60 * 1000)
        // 设置读取超时
        configBuilder.setSocketTimeout(3 * 1000)
        // 设置连接超时
        configBuilder.setConnectTimeout(5 * 1000)

        mRequestConfig = configBuilder.build()
    }

    // 创建SSL安全连接
    private fun createSSLConnSocketFactory(): SSLConnectionSocketFactory {
        val sslContext = SSLContextBuilder()
                .loadTrustMaterial(null) { _, _ -> true }.build()
        return SSLConnectionSocketFactory(sslContext, HostnameVerifier { _, _ -> true })
    }

    // 获取客户端
    @Throws(Exception::class)
    private fun getHttpClient(url: String, cookies: BasicCookieStore? = null): CloseableHttpClient {
        val hcb = HttpClients.custom()
        hcb.setConnectionManager(mConnectionManager)
        hcb.setDefaultRequestConfig(mRequestConfig)
        if (url.toLowerCase().startsWith("https")) {
            hcb.setSSLSocketFactory(createSSLConnSocketFactory())
        }
        if (cookies != null) {
            hcb.setDefaultCookieStore(cookies)
        }
        return hcb.build()
    }

    // 获取客户端
    private fun getHttpClientNoManage(url: String, cookies: BasicCookieStore? = null): CloseableHttpClient {
        val hcb = HttpClients.custom()
        if (url.toLowerCase().startsWith("https")) {
            hcb.setSSLSocketFactory(createSSLConnSocketFactory())
        }
        if (cookies != null) {
            hcb.setDefaultCookieStore(cookies)
        }
        return hcb.build()
    }

    // 统一执行入口 避免连接池异常处理
    private fun executeRequest(url: String, uriRequest: HttpUriRequest, cookies: BasicCookieStore? = null): CloseableHttpResponse {
        val response = try {
            getHttpClient(url, cookies).execute(uriRequest)
        } catch (e: Exception) {
            getHttpClientNoManage(url, cookies).execute(uriRequest)
        }
        if (response != null && response.statusLine.statusCode != 200) {
            uriRequest.abort()
        }
        return response;
    }

    // 处理响应结果
    private fun handlerResponse(response: CloseableHttpResponse?): String? {
        if (response == null) {
            return null;
        }
        val entity = response.entity ?: return null
        try {
            return EntityUtils.toString(entity, "UTF-8")
        } catch (e: Exception) {
            return ObjectMapper().writeValueAsString(mapOf("http_client_error" to e.message))
        }
    }

    fun GET(url: String, header: Map<String, String> = mapOf()): String? {
        val httpGet = HttpGet(url)
        header.forEach {
            httpGet.addHeader(it.key, it.value);
        }
        return handlerResponse(executeRequest(url, httpGet))
    }

    fun POST(url: String, params: Map<String, Any>, header: Map<String, String> = mapOf()): String? {
        val httpPost = HttpPost(url)
        header.forEach {
            httpPost.addHeader(it.key, it.value);
        }
        val pairList = ArrayList<NameValuePair>(params.size)
        for (entry in params.entries) {
            val pair = BasicNameValuePair(entry.key, entry.value.toString())
            pairList.add(pair)
        }
        val requestEntity = UrlEncodedFormEntity(pairList, Charset.forName("UTF-8"));
        httpPost.entity = requestEntity;
        return handlerResponse(executeRequest(url, httpPost))
    }

    fun JSON(url: String, params: Map<String, Any>, header: Map<String, String> = mapOf()): String? {
        val httpPost = HttpPost(url)
        header.forEach {
            httpPost.addHeader(it.key, it.value);
        }
        val json = ObjectMapper().writeValueAsString(params);
        val se = StringEntity(json);
        se.setContentEncoding("UTF-8");
        se.setContentType("application/json");
        httpPost.entity = se;
        return handlerResponse(executeRequest(url, httpPost))
    }

    fun COOKIE(url: String, params: Map<String, Any>, header: Map<String, String> = mapOf(),
                    cookies: BasicCookieStore = BasicCookieStore(), method: String = "post"): String? {
        if (method.toLowerCase() == "get") {
            val httpGet = HttpGet(url)
            header.forEach {
                httpGet.addHeader(it.key, it.value);
            }
            return handlerResponse(executeRequest(url, httpGet, cookies))
        } else {
            val httpPost = HttpPost(url)
            header.forEach {
                httpPost.addHeader(it.key, it.value);
            }
            val pairList = ArrayList<NameValuePair>(params.size)
            for (entry in params.entries) {
                val pair = BasicNameValuePair(entry.key, entry.value.toString())
                pairList.add(pair)
            }
            val requestEntity = UrlEncodedFormEntity(pairList, Charset.forName("UTF-8"));
            httpPost.entity = requestEntity;
            return handlerResponse(executeRequest(url, httpPost, cookies))
        }
    }

}
