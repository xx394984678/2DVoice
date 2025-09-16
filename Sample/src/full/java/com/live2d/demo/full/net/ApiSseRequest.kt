package com.live2d.demo.full.net

import android.util.Log
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.sse.EventSource

/** SSE请求实例*/
object ApiSseRequest {
    private val TAG = ApiSseRequest::class.java.simpleName
    private val mSseEventSource = SseEventSource()

    /**
     * 流式get请求
     *
     * @param apiUrl 请求地址（不带baseUrl)
     * @param isClickRetry 是否是重新生成触发的
     * @param callback 请求相关的回调
     */
    fun get(url: String, isClickRetry: Boolean = false, callback: OnSseCallback): EventSource{
        Log.d(TAG, "sse request url: ${url}")
        val request = Request.Builder()
            .get()
            .url(url)
//            .addHeader("vcinema-notoken", "true")
//            .addHeader("vcinema-nolog", "true")
            .build()
        return mSseEventSource.handleRequest(request, isClickRetry, callback)
    }

// /**
//     * 流式post请求
//     *
//     * @param apiUrl 请求地址（不带baseUrl)
//     * @param isClickRetry 是否是重新生成触发的
//     * @param callback 请求相关的回调
//     */
//    fun post(url: String, isClickRetry: Boolean = false, parameter: String, callback: OnSseCallback): EventSource{
//        LogUtil.d(TAG, "sse request url: ${url}")
//        val request = Request.Builder()
//            .url(url)
//            .post(RequestBody.create(MediaType.parse("text/event-stream"), parameter))
//            .addHeader("vcinema-notoken", "true")
//            .addHeader("vcinema-nolog", "true")
//            .build()
//        return mSseEventSource.handleRequest(request, isClickRetry, callback)
//    }
//
//    /** 停止流数据接收*/
//    fun stop(){
//        mSseEventSource.stop()
//    }



}