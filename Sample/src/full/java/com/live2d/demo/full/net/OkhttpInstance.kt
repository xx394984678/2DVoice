package ai.guiji.duix.test.net

import android.util.Log
import com.live2d.demo.full.net.ApiServer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val ApiInstance = OkhttpInstance().instance
class OkhttpInstance {

    // 基础 URL，通常以 / 结尾
    companion object {
        const val BASE_URL = "https://dev-environmental.vcinema.cn:3093"
    }


    // 惰性初始化 Retrofit 实例
    val instance: ApiServer by lazy {
        Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL) // 设置基础 URL
                .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换工厂，用于解析 JSON
                // .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // 如果需要 RxJava 支持，添加此行
                .build()
                .create(ApiServer::class.java) // 创建 API 接口实例
    }



    private fun getSslContext(x509TrustManager: X509TrustManager): SSLContext {
        val trustAllCerts = arrayOf(x509TrustManager)

        val sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext
    }
    private val myX509TrustManager = object : X509TrustManager {
        /**
         * 初始化一个系统的验证器
         * 系统提供的实现类是 RootTrustManager
         */
        private val defaultTrustManager: X509TrustManager?
        init {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            //自签名证书需要在此初始化自己的证书，ca颁布的需要初始化null
            //内部的factorySpi实现类是RootTrustManagerFactorySpi.java 当传入不为空，会以传入的证书生成配置，传入为空获取默认配置。所以自签名的证书，仅支持验证自家的网站
            trustManagerFactory.init(null as KeyStore?)
            defaultTrustManager = chooseTrustManager(trustManagerFactory.trustManagers)
        }

        private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
            for (trustManager in trustManagers) {
                if (trustManager is X509TrustManager) {
                    return trustManager
                }
            }
            return null
        }

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            if (true) {
                return
            }
            requireNotNull(chain) { "checkServerTrusted:x509Certificate array isnull" }
            require(chain.isNotEmpty()) { "checkServerTrusted: X509Certificate is empty" }

            try {
                defaultTrustManager?.checkServerTrusted(chain, authType)
            } catch (e: Exception) {
                var t = e as? Throwable
                while (t != null) {
                    if (t is CertificateNotYetValidException
                        || t is CertificateExpiredException
                        || t is javax.security.cert.CertificateNotYetValidException
                        || t is javax.security.cert.CertificateExpiredException) {
                        return
                    }
                    t = t.cause
                }
                e.printStackTrace()
                throw Exception(e)
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }

    }

    val client = OkHttpClient.Builder()
            .sslSocketFactory(getSslContext(myX509TrustManager).socketFactory, myX509TrustManager)
            .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                Log.d("HttpLogDetail", message) }))
            .build()
}