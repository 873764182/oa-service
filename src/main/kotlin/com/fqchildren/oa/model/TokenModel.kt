package com.fqchildren.oa.model

import com.fqchildren.oa.base.BaseModel
import com.fqchildren.oa.utils.PassUtils

/**
 * Token 数据模型
 */
class TokenModel private constructor(
        var uid: String = "",   // 用户唯一标识
        var did: String = "",   // 设备唯一标识
        var vt: Long = 0L       // 有效截止日期（为了降低长度，使用秒）
) : BaseModel() {

    fun getTokenString(): String {
        val result = encryptToken(this);
        return result ?: "";
    }

    fun notTimeout(): Boolean {
        return notTimeout(this);
    }

    override fun toString(): String {
        return "$uid,$did,$vt";
    }

    companion object {
        private const val attribute_separator = "&&&";  // 属性分隔符
        private const val encrypt_password = "@token01";  // 加密密码
        private const val default_vt = (30 * 24 * 60 * 60 * 1000L);  // 默认有效时间30天

        /**
         * 加密支付生成token数据
         */
        fun encryptToken(tokenModel: TokenModel): String? {
            val tokenString = tokenModel.uid + attribute_separator +
                    tokenModel.did + attribute_separator + tokenModel.vt;
            return PassUtils.encryptAes(tokenString.trim(), encrypt_password);
        }

        /**
         * 解密数据还原token数据
         */
        fun decryptToken(tokenString: String): TokenModel? {
            if (tokenString.isEmpty()) {
                return null;
            }
            val ts = PassUtils.decryptAes(tokenString.trim(), encrypt_password)
                    ?: return null;
            val tsa = ts.split(attribute_separator);
            if (tsa.size != 3) {
                return null;
            }
            val uid = tsa[0];
            if (uid.isEmpty()) {
                return null;
            }
            val did = tsa[1];
            if (did.isEmpty()) {
                return null;
            }
            val vt = tsa[2];
            if (vt.isEmpty()) {
                return null;
            }
            return TokenModel(uid = uid, did = did, vt = vt.toLong());
        }

        /**
         * 是否超时
         */
        fun notTimeout(tokenModel: TokenModel): Boolean {
            return tokenModel.vt > (System.currentTimeMillis() / 1000);
        }

        /**
         * 创建一个Token对象
         */
        fun createToken(uid: String, did: String = "", vt: Long = (System.currentTimeMillis() + default_vt)): TokenModel {
            return TokenModel(uid, did, vt);
        }
    }
}
