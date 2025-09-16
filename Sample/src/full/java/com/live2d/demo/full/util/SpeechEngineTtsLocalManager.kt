package com.live2d.demo.full.util

import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.bytedance.speech.speechengine.SpeechEngine
import com.bytedance.speech.speechengine.SpeechEngine.SpeechListener
import com.bytedance.speech.speechengine.SpeechEngineDefines
import com.bytedance.speech.speechengine.SpeechEngineGenerator
import java.util.LinkedList
import java.util.Queue

/**
 * Created by Wang Ya Fei
 * Date：2024/8/21 16:10
 * Description：火山引擎语音合成
 **/
class SpeechEngineTtsLocalManager{
    val TAG = "aiChatDebug"
    /**
     * SDK 内部核心 SpeechEngine 类
     */
    private var mSpeechEngine: SpeechEngine? = null
    private var mSpeechVoiceTypeOnlineString: String = "zh_male_beijingxiaoye_moon_bigtts"


    /**需要合成的语音 数据流 */
     var mDataQueue: Queue<String> = LinkedList()

    //播放繁忙
    private var mTtsSynthesisFromPlayer:Boolean = false
    //合成繁忙
    private var mTtsSynthesisFromSynthesis:Boolean = false
    private var mCurTtsText: String = ""
    private var mLastTtsDataId: String = ""

    var mInit = false
    //SSE流是否结束
    var mStreamEnd = true
    //引擎是否关闭
    var mSpeechEngineClose = true
    //语音合成是否播报结束
    var mSpeechFinish = true
    //是不是单次合成
     var mScenarioTypeNormal = false

    //语音合成结果监听
    var listener: OnSpeechSynthesizerListener? = null

    private var context: Application? = null
    fun initSDK(context: Application) {
        this.context = context

        SpeechEngineGenerator.PrepareEnvironment(context,context)
        mSpeechEngine = SpeechEngineGenerator.getInstance().apply {
            createEngine()
            // 语音合成引擎
            setOptionString(SpeechEngineDefines.PARAMS_KEY_ENGINE_NAME_STRING, SpeechEngineDefines.TTS_ENGINE)
            setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_WORK_MODE_INT, SpeechEngineDefines.TTS_WORK_MODE_ONLINE)

//            setContext(context)
            setOptionString(SpeechEngineDefines.PARAMS_KEY_UID_STRING, "user")
            // 在线授权
            //【必须配置】鉴权相关：AppID
            setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_ID_STRING, SpeechAuthUtil.appId)
            //【必须配置】鉴权相关：Token
            setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_TOKEN_STRING, SpeechAuthUtil.jwtToken)

            // 在线请求资源配置
            setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_ADDRESS_STRING, "wss://openspeech.bytedance.com")
            //【必需配置】语音合成服务Uri
            setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_URI_STRING, "/api/v1/tts/ws_binary")
            //【必需配置】语音合成服务所用集群
            setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_CLUSTER_STRING, SpeechAuthUtil.getTTSCluster())

           val ret =  initEngine()
            if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
                val message = "initEngine, " + ret
                Log.d(TAG,message)
            }
            setListener(eventAsrListener)

        }
        mInit = true

    }
    fun speak(text:String){
       mSpeechEngine?.apply {
           //【必需配置】TTS 使用场景 单次合成场景
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_SCENARIO_STRING, SpeechEngineDefines.TTS_SCENARIO_TYPE_NORMAL)
           // 在线合成使用的“发音人”
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_VOICE_ONLINE_STRING, "other")
           // 在线合成使用的“演绎风格” 音色代号
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_VOICE_TYPE_ONLINE_STRING,  "zh_male_beijingxiaoye_moon_bigtts")
//           // 音色对应音高
           setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_PITCH_INT, SpeechAuthUtil.mSpeechPitchInt)
//           // 音色对应音量
           setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_VOLUME_INT, SpeechAuthUtil.mSpeechVolumeInt)
//           // 音色对应语速
           setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_SPEED_INT, SpeechAuthUtil.mSpeechSpeedInt)
           //【必需配置】需合成的文本，不可超过 80 字
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_TEXT_STRING, text)
          val ret = sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, "");
           if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
               val message = "发送启动引擎指令失败, " + ret
               Log.d(TAG,message)
           }
       }
   }
    private fun speechFinishSynthesis() {
        //处理合成繁忙 错误码
        if (mTtsSynthesisFromSynthesis) {
            mTtsSynthesisFromSynthesis = false
            if (!TextUtils.isEmpty(mCurTtsText)){
                Log.d(TAG,"处理合成繁忙, "+mCurTtsText)
                speakNovel(mCurTtsText)
            }
            return
        }
        //正常合成
        if (mDataQueue.peek() == null){
            Log.d(TAG,"mDataQueue 队列没数据了")
            return
        }
        val data = mDataQueue.poll()
        if (TextUtils.isEmpty(data)){
            Log.d(TAG,"mDataQueue 取出的数据为空字符串")
            speechFinishSynthesis()
            return
        }
        Log.d(TAG,"正常合成, "+data)
        speakNovel(data!!)
    }


    private fun speechFinishPlaying() {
        if (mTtsSynthesisFromPlayer) {
            mTtsSynthesisFromPlayer = false
            if (!TextUtils.isEmpty(mCurTtsText)){
                Log.d(TAG,"处理播放繁忙, "+mCurTtsText)
                speakNovel(mCurTtsText)
            }
        }
    }


    /** 添加说话内容 **/
    fun addSpeakData(mCurTtsText:String){

        if(mCurTtsText.isNullOrEmpty()){
            return
        }

        if (mDataQueue.isEmpty()){
            mDataQueue.add("")
            Log.d(TAG,"开始合成, ")
            mSpeechEngine?.sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, "")
            speakNovel(mCurTtsText)
            return
        }
        mDataQueue.add(mCurTtsText)
    }


   private fun speakNovel(text:String){
       mCurTtsText = ""
       mTtsSynthesisFromPlayer = false
       mTtsSynthesisFromSynthesis = false
       mSpeechEngine?.apply {
           //【必需配置】TTS 使用场景 单次合成场景
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_SCENARIO_STRING, SpeechEngineDefines.TTS_SCENARIO_TYPE_NOVEL)
           // 在线合成使用的“发音人”
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_VOICE_ONLINE_STRING, "other")
           // 在线合成使用的“演绎风格” 音色代号
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_VOICE_TYPE_ONLINE_STRING,  "zh_male_beijingxiaoye_moon_bigtts")
        // 音色对应音高
           setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_PITCH_INT, SpeechAuthUtil.mSpeechPitchInt)
//           // 音色对应音量
           setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_VOLUME_INT, SpeechAuthUtil.mSpeechVolumeInt)
//           // 音色对应语速
           setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_SPEED_INT, SpeechAuthUtil.mSpeechSpeedInt)
           //【必需配置】需合成的文本，不可超过 80 字
           setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_TEXT_STRING, text)
           Log.d(TAG,"发送合成文本, "+text)
           val ret = sendDirective(SpeechEngineDefines.DIRECTIVE_SYNTHESIS, "")
           if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
               val message = "发送启动引擎指令失败, " + ret
               Log.d(TAG,message)
               mCurTtsText = text
               if( ret==SpeechEngineDefines.ERR_SYNTHESIS_PLAYER_IS_BUSY){
                   mTtsSynthesisFromPlayer = true
               }
               if( ret==SpeechEngineDefines.ERR_SYNTHESIS_IS_BUSY){
                   mTtsSynthesisFromSynthesis = true
               }
           }
       }
   }


    fun setSpeechVoiceTypeOnlineString(voiceType: String){
        if (TextUtils.isEmpty(voiceType)){
            return
        }
        mSpeechVoiceTypeOnlineString = voiceType
//        SPUtils.getInstance().saveString(Constants.AI_ASSISTANT_CURRENT_TIMBRE,voiceType)
    }

    private var eventAsrListener: SpeechListener = SpeechListener { type, data, length ->
        val stdData =  String(data)
        when (type) {
            SpeechEngineDefines.MESSAGE_TYPE_ENGINE_START -> {
                // Callback: 引擎启动成功回调
                Log.d(TAG, "Callback: 引擎启动成功: data: $stdData")
                mSpeechEngineClose = false
                mSpeechFinish = false
            }
            SpeechEngineDefines.MESSAGE_TYPE_ENGINE_STOP -> {
                // Callback: 引擎关闭回调
                mSpeechEngineClose = true
                mSpeechFinish = true
                mDataQueue.clear()
                Log.d(TAG, "Callback: 引擎关闭: data: $stdData")
//                if (VolumeUtil.getVolumeMusic().second ==0 && mStreamEnd){
//                    listener?.onSpeechFinish(stdData)
//                }
                listener?.onSpeechFinish(stdData)

            }
            SpeechEngineDefines.MESSAGE_TYPE_ENGINE_ERROR -> {
                listener?.onSpeechError()
                mSpeechEngineClose = true
                mSpeechFinish = true
                mDataQueue.clear()
                // Callback: 错误信息回调
                Log.e(TAG, "Callback: 错误信息: $stdData")
            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_SYNTHESIS_BEGIN -> {
                // Callback: 合成开始回调
                Log.e(TAG, "Callback: 合成开始: $stdData")
                mSpeechFinish = false

            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_SYNTHESIS_END -> {
                // Callback: 合成结束回调
                Log.e(TAG, "Callback: 合成结束: $stdData")
                mLastTtsDataId = stdData
                speechFinishSynthesis()
            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_START_PLAYING -> {
                // Callback: 播放开始回调
                Log.e(TAG, "Callback: 播放开始: $stdData")
                listener?.onSpeechStart(stdData)

            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_PLAYBACK_PROGRESS -> {
                // Callback: 播放进度回调
                Log.e(TAG, "Callback: 播放进度")
            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_FINISH_PLAYING -> {
                // Callback: 播放结束回调
                Log.e(TAG, "Callback: 播放结束: $stdData")
                if (mScenarioTypeNormal){
                    Log.e(TAG, "Callback: 所有文本播放结束！")
                    listener?.onSpeechFinish(stdData)
                    mSpeechFinish = true
                    return@SpeechListener
                }
                speechFinishPlaying()
                if (TextUtils.equals(mLastTtsDataId,stdData) && mStreamEnd&& mDataQueue.isEmpty()){
                    Log.e(TAG, "Callback: 所有文本播放结束！")
                    listener?.onSpeechFinish(stdData)
                    mSpeechFinish = true
                }
            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_AUDIO_DATA -> {
                // Callback: 音频数据回调
                Log.e(TAG, "Callback: 音频数据，长度 ${stdData.length} 字节")
            }
            SpeechEngineDefines.MESSAGE_TYPE_TTS_AUDIO_DATA_END -> {
                // Callback: 音频数据回调
                Log.e(TAG, "Callback: 音频数据，长度 0 字节")
            }
            else ->{}
        }
    }

    /**
     * 释放引擎
     */
    fun release(){
        Log.d(TAG ,"---release--->释放引擎")
        if (mSpeechEngine != null) {
            mSpeechEngine?.destroyEngine()
        }
        mSpeechEngine = null
        mInit = false
    }
    /**
     * 暂停合成
     */
    fun pause(){
        Log.d(TAG ,"---pause--->暂停合成")

        if (!mInit){
            return
        }
        mSpeechEngine?.sendDirective(SpeechEngineDefines.DIRECTIVE_PAUSE_PLAYER, "")
    }
    /**
     *恢复合成
     */
    fun resume(){
        Log.d(TAG ,"---resume--->恢复合成")
        if (!mInit){
            return
        }
        mSpeechEngine?.sendDirective(SpeechEngineDefines.DIRECTIVE_RESUME_PLAYER, "")
    }

    /**
     * 取消合成
     */
    fun cancelSynthesizer(){
        Log.d(TAG ,"---cancelSynthesizer--->取消合成")
        if (!mInit){
            return
        }
        mCurTtsText = ""
        mSpeechFinish = true
        mDataQueue.clear()
        mSpeechEngine?.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "")
        mSpeechEngine?.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNC_STOP_ENGINE, "")
    }

}