package com.live2d.demo.full.net

import ai.guiji.duix.test.net.OkhttpInstance
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.live2d.demo.full.entity.ApiServerEntity
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

/**
 * 通用流处理
 */
open class SseEventSource {
    private val TAG = SseEventSource::class.java.simpleName
    private val factory: EventSource.Factory = EventSources.createFactory(OkhttpInstance().client)
    private var mNewEventSource: EventSource? = null
    private val mMainHandler = Handler(Looper.getMainLooper())


    /** 是否是手动取消的*/
    private var mIsCanceled: Boolean = false

    /**
     * 处理请求
     * @param isRetry 是否是重新发送触发的
     */
    open fun handleRequest(request: Request, isClickRetry: Boolean = false, callback: OnSseCallback?): EventSource{
        mNewEventSource?.cancel() // 把上个流取消掉
        mIsCanceled = false
        mNewEventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                super.onOpen(eventSource, response)
                Log.d(TAG, "onOpen")
                mMainHandler.post {
                    callback?.onSendSuccess()
                }
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                super.onEvent(eventSource, id, type, data)
                try {
                    Log.d(TAG, "onEvent >> 接收到的数据: " + data)
                    val entity = Gson().fromJson(data, ApiServerEntity::class.java)

                    callback?.onEventSuccess(entity)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // 如果异常就等待下次接收来处理 - 如果真的有连接异常会在【onFailure】回调
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                super.onFailure(eventSource, t, response)
//                LogUtil.d(TAG, "isCanceled： ${mIsCanceled} >> onFailure >> " + t)
//                // 其他错误就正常情况
//                // 取消就不走回调错误回调
//                if (mIsCanceled || t is StreamResetException) {
//                    LogUtil.d(TAG, "11 - isCanceled： ${mIsCanceled} >> onFailure >> " + t)
//                    mMainHandler.post {
//                        try {
//                            response?.close()
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                        callback?.onCancel(isClickRetry)
//                    }
//                    mIsCanceled = false
//                    return
//                }
//                LogUtil.d(TAG, "222 - isCanceled： ${mIsCanceled} >> onFailure >> " + t)
//                mMainHandler.post {
//                    callback?.onFail(isClickRetry)
//                }
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
//                LogUtil.d(TAG, "onClosed")
//                mMainHandler.post {
//                    callback?.onClose()
//                }
            }
        })
        return mNewEventSource!!
    }

    /** 停止返回流*/
    fun stop(){
        if (mNewEventSource != null) {
            mIsCanceled = true
            mNewEventSource?.cancel()
        }
    }


}