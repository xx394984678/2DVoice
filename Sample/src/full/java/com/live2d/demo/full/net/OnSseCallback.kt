package com.live2d.demo.full.net

import com.live2d.demo.full.entity.ApiServerEntity


/** 问答的回调*/
interface OnSseCallback {

    /** 发送成功*/
    fun onSendSuccess()

    /** 回复流事件*/
    fun onEventSuccess(eventContent: ApiServerEntity?)

    /**
     * 回复出错
     * @param isClickRetry 是否是重试生成
     */
    fun onFail(isClickRetry: Boolean = false)

    fun onOauthVerityFailed()

    /**
     * 终止请求的回调
     * @param isClickRetry 是否是重新生成的方式
     */
    fun onCancel(isClickRetry: Boolean = false)

    /**
     * 流正常关闭情况
     */
    fun onClose()

}