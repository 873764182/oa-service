package com.fqchildren.oa.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

/**
 * 密码相关工具对象
 */
object PassUtils {

    // 密码秘钥 固定为8位数 www.fqchildren.com
    private const val CIPHER_KEY = "children";

    /**
     * 加密 (用户登录密码用这个加密)
     */
    fun encryptAes(content: String, password: String = CIPHER_KEY): String {
        try {
            val kgen = KeyGenerator.getInstance("AES")
            kgen.init(128, SecureRandom(password.toByteArray()))
            val secretKey = kgen.generateKey()
            val enCodeFormat = secretKey.encoded
            val key = SecretKeySpec(enCodeFormat, "AES")
            val cipher = Cipher.getInstance("AES")// 创建密码器
            val byteContent = content.toByteArray(charset("utf-8"))
            cipher.init(Cipher.ENCRYPT_MODE, key)// 初始化
            val result = cipher.doFinal(byteContent)
            // return result; // 如果加密解密有误 请直接返回加密后的二进制
            val sb = StringBuilder()
            for (aResult in result) {
                var hex = Integer.toHexString(aResult.toInt() and 0xFF)
                if (hex.length == 1) {
                    hex = "0$hex"
                }
                sb.append(hex.toUpperCase())
            }
            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 解密
     */
    fun decryptAes(content: String, password: String = CIPHER_KEY): String? {
        try {
            if (content.isEmpty()) {
                return null
            }
            val result = ByteArray(content.length / 2)
            for (i in 0 until content.length / 2) {
                val high = Integer.parseInt(content.substring(i * 2, i * 2 + 1), 16)
                val low = Integer.parseInt(content.substring(i * 2 + 1, i * 2 + 2), 16)
                result[i] = (high * 16 + low).toByte()
            }
            // 如果加密解密有误 请直接从byte[]解析
            val kgen = KeyGenerator.getInstance("AES")
            kgen.init(128, SecureRandom(password.toByteArray()))
            val secretKey = kgen.generateKey()
            val enCodeFormat = secretKey.encoded
            val key = SecretKeySpec(enCodeFormat, "AES")
            val cipher = Cipher.getInstance("AES")// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key)// 初始化
            val value = cipher.doFinal(result)
            return String(value) // 加密
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 计算MD5值
     */
    fun md5(value: String): String {
        try {
            val instance = MessageDigest.getInstance("MD5") // 获取md5加密对象
            val digest = instance.digest(value.toByteArray()) // 对字符串加密，返回字节数组
            val sb = StringBuffer()
            for (b in digest) {
                val i: Int = b.toInt() and 0xff // 获取低八位有效值
                var hexString = Integer.toHexString(i) // 将整数转化为16进制
                if (hexString.length < 2) {
                    hexString = "0$hexString" // 如果是一位的话，补0
                }
                sb.append(hexString)
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

}
