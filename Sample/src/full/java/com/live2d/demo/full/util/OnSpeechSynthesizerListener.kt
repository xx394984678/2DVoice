package com.live2d.demo.full.util

/**
 * Created by Wang Ya Fei
 * Date：2022/12/9 2:32 下午
 * Description：语音合成结果回调
 **/
interface OnSpeechSynthesizerListener {
    fun onSpeechStart(speechContent:String?)
    fun onSpeechFinish(utteranceId: String?)
    fun onSpeechError()

}