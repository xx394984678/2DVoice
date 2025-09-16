package com.live2d.demo.full.rtc

import ai.guiji.duix.test.net.ApiInstance
import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.live2d.demo.full.audio.PCMAudioPlayer
import com.live2d.demo.full.entity.ApiServerEntity
import com.live2d.demo.full.audio.WavUtils
import com.live2d.demo.full.entity.CreateRoomEntity
import com.live2d.demo.full.entity.JoinRoom
import com.live2d.demo.full.entity.SubtitleMsgData
import com.live2d.demo.full.entity.SubtitleWrapEntity
import com.live2d.demo.full.entity.VoiceTrainStatusEntity
import com.live2d.demo.full.net.ApiSseRequest
import com.live2d.demo.full.net.OnSseCallback
import com.live2d.demo.full.net.SseRequest
import com.live2d.demo.full.util.OnSpeechSynthesizerListener
import com.live2d.demo.full.util.SpeechEngineTtsLocalManager
import com.ss.bytertc.engine.IAudioFrameProcessor
import com.ss.bytertc.engine.RTCRoom
import com.ss.bytertc.engine.RTCRoomConfig
import com.ss.bytertc.engine.RTCVideo
import com.ss.bytertc.engine.UserInfo
import com.ss.bytertc.engine.data.AudioChannel
import com.ss.bytertc.engine.data.AudioFormat
import com.ss.bytertc.engine.data.AudioProcessorMethod
import com.ss.bytertc.engine.data.AudioSampleRate
import com.ss.bytertc.engine.data.RemoteAudioState
import com.ss.bytertc.engine.data.RemoteAudioStateChangeReason
import com.ss.bytertc.engine.data.RemoteStreamKey
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler
import com.ss.bytertc.engine.handler.IRTCVideoEventHandler
import com.ss.bytertc.engine.type.ChannelProfile
import com.ss.bytertc.engine.type.MediaStreamType
import com.ss.bytertc.engine.type.MessageConfig
import com.ss.bytertc.engine.utils.IAudioFrame
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.HashMap
import kotlin.random.Random

class RtcManager(val context:Context,private val rtcListener: RtcListener) {

    interface RtcListener {
        fun onStartTalk()
        fun onStopTalk()
        fun onMoodSwings(number: Int)//情绪变化
        fun talkingText(text: String)//情绪变化
    }

    val TAG = "RtcManager"
    private lateinit var rtcVideo: RTCVideo
    private lateinit var rtcRoom: RTCRoom

    var APP_ID: String = "68abf5bf7dba7e0175ca4c59" // 填写 appId
    var roomId: String = "" // 填写房间号
    var userId: String = "111111" //填写 userId
    var token: String = "" // 填写临时 token

    fun requestPermission(activity: Activity) {
        val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.RECORD_AUDIO,
                                          Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                          Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
            ) {
                activity.requestPermissions(PERMISSIONS_STORAGE, 22)
            }
        }
    }

    /**
     * 设置本地渲染视图，支持TextureView和SurfaceView
     */
    private fun setLocalRenderView() {
//        val textureView = TextureView(this)
//        localViewContainer.removeAllViews()
//        localViewContainer.addView(textureView)

//        val videoCanvas: VideoCanvas = VideoCanvas()
//        videoCanvas.renderView = textureView
//        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
//         设置本地视频渲染视图
//        rtcVideo.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas)
    }

    private fun setRemoteRenderView(uid: String) {
//        val remoteTextureView = TextureView(this)
//        remoteViewContainer.removeAllViews()
//        remoteViewContainer.addView(remoteTextureView)
//        val videoCanvas: VideoCanvas = VideoCanvas()
//        videoCanvas.renderView = remoteTextureView
//        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
//
//        val remoteStreamKey: RemoteStreamKey = RemoteStreamKey(roomId, uid, StreamIndex.STREAM_INDEX_MAIN)
//         设置远端视频渲染视图
//        rtcVideo.setRemoteVideoCanvas(remoteStreamKey, videoCanvas)
    }

    public fun createRtc(activity: Activity){

        // 创建引擎
        rtcVideo = RTCVideo.createRTCVideo(activity, APP_ID, videoEventHandler, null, null);
        // 设置本端渲染视图
        setLocalRenderView();
        // 开启音视频采集
        rtcVideo.startVideoCapture();
        rtcVideo.startAudioCapture();



        speakManager = SpeechEngineTtsLocalManager()
        speakManager?.initSDK(activity.application)
        speakManager?.listener = object : OnSpeechSynthesizerListener{
            override fun onSpeechStart(speechContent: String?) {
            }

            override fun onSpeechFinish(utteranceId: String?) {
                //语音播报完成

                if(isCollectFinish){
                    sendLLMMessage("BotName001",apiResult.toString())
                }

            }

            override fun onSpeechError() {
            }

        }


    }
    /**
     * 引擎回调信息
     */
    var videoEventHandler: IRTCVideoEventHandler = object : IRTCVideoEventHandler() {
        override fun onRemoteAudioStateChanged(key: RemoteStreamKey?, state: RemoteAudioState?, reason: RemoteAudioStateChangeReason?) {
            super.onRemoteAudioStateChanged(key, state, reason)
            Log.d(TAG, "onRemoteAudioStateChanged: $key $state $reason")
        }
    }

    var rtcRoomEventHandler: IRTCRoomEventHandler = object : IRTCRoomEventHandler() {
        override fun onUserPublishStream(uid: String, type: MediaStreamType?) {
            super.onUserPublishStream(uid, type)
//            runOnUiThread { setRemoteRenderView(uid) }
        }

        /**
         * 配置完成后，在真人用户与智能体对话期间，你可通过 onRoomBinaryMessageReceived （硬件场景下使用on_message_received）回调接收字幕结果。该回调中的 message 字段中的内容为字幕结果，格式为二进制，使用前需解析。
         */
        override fun onRoomBinaryMessageReceived(uid: String?, message: ByteBuffer) {
            val subtitles = StringBuilder()
            val ret = SubtitleWrapEntity.unpack(message, subtitles)
            if (ret) {
                parseData(subtitles.toString())
            }
        }
    }



    var rtcIsStart = false

    var stringBuilder = StringBuilder()

    var isAllSpeakEnd = false

    /**
     * 来源：根据 SubtitleMode 模式的不同，来源为 LLM 模块生成内容或 TTS 模块朗读内容。
     * 字幕返回方式 ：
     * 流式返回说话内容。存在分句和整句的区别。例如：分句1：上海天气炎热。分句2：气温为 30 摄氏度。整句：上海天气炎热。气温为 30 摄氏度。
     * 当新的分句开始时，字幕不会显示上一个已结束的分句。
     * 说话过程中：
     * definite ：可能为 false （表示当前分句尚未结束）或 true （表示当前分句已结束，但一整句话还未结束）。
     * paragraph：始终为 false 。表示一整句还未结束。
     * 说话结束时 ：
     * definite ：为 true ，表示分句已结束。
     * paragraph ：为 true ，表示一整句已结束。
     */
    private fun parseData(msg: String) {
        try {
//            Log.d(TAG, "parseData: ${msg}")
            val subtitleWrapEntity = Gson().fromJson(msg, SubtitleWrapEntity::class.java)
            if (subtitleWrapEntity.type == "subtitle") {
                //字幕类型
                val subtitleEntity = subtitleWrapEntity.data.get(0)
                rtcListener.talkingText(subtitleEntity.text)


                stringBuilder.append(subtitleEntity.text)



                if (subtitleEntity.userId == userId) {
                    if (subtitleEntity.paragraph == true) {
                        //整句 结束
                        stopPush()
                    } else {
                        startRecording()
                    }
                }else{
                    if (subtitleEntity.paragraph == true) {
                        //整句 结束
                        Log.d(TAG, "parseData: 结束push")
                        rtcListener.onStopTalk()

                        if(isAutoSpeak){
                            askQuestionToAI("111",stringBuilder.toString())
                        }
                        stringBuilder.clear()

                    } else {
                        Log.d(TAG, "parseData: ${subtitleEntity.text}")
                        rtcListener.onStartTalk()
                    }
                    onNewSpeak(subtitleEntity)
                }
            } else {
                Log.d(TAG, "parseData: 其他类型:${subtitleWrapEntity.type}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var thisSpeakIsTalking = false
    //情绪字符的长度
    private fun onNewSpeak(subtitleEntity: SubtitleMsgData){
        //这是一个完整分句
        if (subtitleEntity.definite) {
            Log.d(TAG, "分句: 这个分句完成了")
            thisSpeakIsTalking = false
        } else {
            val speakText = subtitleEntity.text
            val startChar = "{"
            val endChar = "}"
            if (!speakText.contains(startChar) || !speakText.contains(endChar)) {
                //不包含必要的情绪信息。返回
                return
            }
            if (thisSpeakIsTalking){
                return
            }
            thisSpeakIsTalking = true
            val leftText = subtitleEntity.text.substring(speakText.indexOf(startChar), speakText.indexOf(endChar)+1)
            Log.d(TAG, "处理情绪: ${subtitleEntity.text}---我截取的${leftText}")
            //这是一个新的分句
            //进行情绪处理
            when (leftText) {
                "{眨眼}" -> {
                    Log.d(TAG, "动作: ")

                }
                "{惊奇}" -> {
                    Log.d(TAG, "动作: 1")
                    rtcListener.onMoodSwings(1)
                }
                "{点头}","{摇头}","{歪头}","{撒娇}" -> {
                    Log.d(TAG, "动作: 2")
                    rtcListener.onMoodSwings(2)
                }
                "{微笑}","{笑}","{大笑}" -> {
                    if (Random.nextInt(3) != 2) {
                        rtcListener.onMoodSwings(3)
                    } else {
                        rtcListener.onMoodSwings(6)//抬胳膊呢
                    }
                    Log.d(TAG, "动作: 笑")
                }
                "{甜笑}" -> {
                    rtcListener.onMoodSwings(6)
                    Log.d(TAG, "动作: 6")
                }
                "{招手}","{害羞}","{调皮吐舌}" -> {
                    rtcListener.onMoodSwings(4)//抬胳膊呢
                    Log.d(TAG, "动作: 4")
                }
                "{生气跺脚}" -> {
                    rtcListener.onMoodSwings(5)
                    Log.d(TAG, "动作: 5")
                }
                "{小声嘟囔}" -> {
                    rtcListener.onMoodSwings(7)
                    Log.d(TAG, "动作: 7")
                }
                "{伸懒腰}","{叹气}" -> {
                    rtcListener.onMoodSwings(8)
                    Log.d(TAG, "动作: 8")
                }

                else -> {}
            }
        }
    }

    fun createRoom(){
        Log.d(TAG, "createRoom: ")
        ApiInstance.createRoom(userId).enqueue(object : Callback<CreateRoomEntity> {
            override fun onResponse(call: Call<CreateRoomEntity>, response: Response<CreateRoomEntity>) {
                roomId = response.body()?.content?.room_id ?: ""
                token = response.body()?.content?.room_token ?: ""
                if (roomId.isNotEmpty()) {
                    Log.d(TAG, "createRoom: $roomId")
                    startRoom()
                }
            }

            override fun onFailure(call: Call<CreateRoomEntity>, t: Throwable) {
                Log.e(TAG, "createRoom: $roomId")
                t.printStackTrace()
            }
        })
    }

    private fun startRoom(){
        ApiInstance.joinRoom(roomId,userId,"ICL_zh_female_tiaopigongzhu_tob").enqueue(object : Callback<JoinRoom> {
            override fun onResponse(call: Call<JoinRoom>, response: Response<JoinRoom>) {
                Log.d(TAG, "startRoom: $roomId")
                joinRoom(roomId)
            }

            override fun onFailure(call: Call<JoinRoom>, t: Throwable) {

            }
        })
    }

    private fun stopRoom(){
        ApiInstance.stopRoom().enqueue(object : Callback<CreateRoomEntity> {
            override fun onResponse(call: Call<CreateRoomEntity>, response: Response<CreateRoomEntity>) {

            }

            override fun onFailure(call: Call<CreateRoomEntity>, t: Throwable) {

            }
        })
    }

    /**
     * 加入房间
     * @param roomId
     */
    private fun joinRoom(roomId: String) {
        Log.d(TAG, "joinRoom create: $roomId")
        rtcRoom = rtcVideo.createRTCRoom(roomId)
        rtcRoom.setRTCRoomEventHandler(rtcRoomEventHandler)
        // 用户信息
        val userInfo: UserInfo = UserInfo(userId, "")
        // 设置房间配置
        val isAutoPublish = true
        val isAutoSubscribeAudio = true
        val isAutoSubscribeVideo = true
        val roomConfig: RTCRoomConfig = RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_CHAT_ROOM, isAutoPublish, isAutoSubscribeAudio, isAutoSubscribeVideo)
        // 加入房间
        rtcRoom.joinRoom(token, userInfo, roomConfig)
        rtcVideo.registerAudioProcessor(object : IAudioFrameProcessor {
            override fun onProcessRecordAudioFrame(audioFrame: IAudioFrame?): Int {
//                选择本地采集的音频时，会收到 onProcessRecordAudioFrame。
                if (audioFrame == null) {
                    Log.d(TAG, "有为空的东西")
                    return 0
                }
                val directBuffer = audioFrame.dataBuffer
                val length: Int = directBuffer.remaining() // 获取可读字节数
                val pcmDataArray = ByteArray(length)
                directBuffer.get(pcmDataArray);
                directBuffer.clear();
                pushPcm(pcmDataArray)
                return 0
            }

            override fun onProcessPlayBackAudioFrame(audioFrame: IAudioFrame?): Int {
//                选择远端音频流的混音音频时，会收到 onProcessPlayBackAudioFrame。
                return 0
            }

            override fun onProcessRemoteUserAudioFrame(streamKey: RemoteStreamKey?, audioFrame: IAudioFrame?): Int {
//                回调单个远端用户的音频帧地址，供自定义音频处理。
                if (streamKey == null || audioFrame == null) {
                    Log.d(TAG, "有为空的东西")
                    return 0
                }
//                Log.d(TAG, "time: ${audioFrame.timestamp_us()}")
//                Log.d(TAG, "sample_rate: ${audioFrame.sample_rate()}")
//                Log.d(TAG, "channel: ${audioFrame.channel()}")
//                Log.d(TAG, "byte_buffer: ${audioFrame.getDataBuffer()}")
//                Log.d(TAG, "data_size: ${audioFrame.data_size()}")
//                val directBuffer = audioFrame.dataBuffer
//                val length: Int = directBuffer.remaining() // 获取可读字节数
//                val pcmDataArray = ByteArray(length)
//                directBuffer.get(pcmDataArray);
//                directBuffer.clear();
//                pushData(pcmDataArray)
                return 0
            }

            override fun onProcessEarMonitorAudioFrame(audioFrame: IAudioFrame?): Int {
                return 0
            }

            override fun onProcessScreenAudioFrame(audioFrame: IAudioFrame?): Int {
                return 0
            }
        })
        /**
         * AUDIO_FRAME_PROCESSOR_RECORD	0	本地采集的音频。
         * AUDIO_FRAME_PROCESSOR_PLAYBACK	1	远端音频流的混音音频。
         * AUDIO_FRAME_PROCESSOR_REMOTE_USER	2	各个远端音频流。
         * AUDIO_FRAME_PROCESSOR_EAR_MONITOR	3	软件耳返音频。
         * AUDIO_FRAME_PROCESSOR_SCREEN	4	屏幕共享音频。
         */
        /**
         * 单次回调的音频帧中包含的采样点数。你可以通过设置 samplesPerCall，sampleRate 和 channel，设置音频帧长。
         *
         * 默认值为 0，此时，采样点数取最小值。最小值为帧长间隔是 0.01s 时的值，即 sampleRate * channel * 0.01s。
         *
         * 最大值是 2048。超出取值范围时，采样点数取默认值。
         *
         * 该参数仅在设置读写回调时生效，调用 enableAudioFrameCallback 开启只读模式回调时设置该参数不生效。
         */
        //设置并开启指定的音频帧回调，进行自定义处理。
//        rtcVideo.enableAudioProcessor(AudioProcessorMethod.AUDIO_FRAME_PROCESSOR_REMOTE_USER, com.ss.bytertc.engine.data.AudioFormat(AudioSampleRate.AUDIO_SAMPLE_RATE_16000, AudioChannel.AUDIO_CHANNEL_MONO, 1280)) // 10ms 320))
        rtcVideo.enableAudioProcessor(AudioProcessorMethod.AUDIO_FRAME_PROCESSOR_RECORD, AudioFormat(AudioSampleRate.AUDIO_SAMPLE_RATE_32000, AudioChannel.AUDIO_CHANNEL_MONO, 1280)) // 10ms 320))
//        rtcVideo.setPlaybackVolume(0)
    }

    /**
     * 离开房间
     */
    fun leaveRoom() {
        if (rtcRoom != null) {
            rtcRoom.leaveRoom()
            rtcRoom.destroy()
        }
    }

    var fileOutPutStream :FileOutputStream? = null
    val pcmName: String = "user_recoder.pcm"

    var recordedTime = 0
    var cacheTime = 0L
    private fun startRecording(){
        //判定一下存储
        if (ContextCompat.checkSelfPermission(context,
                                              Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //error
            
            return
        }
        if (fileOutPutStream != null) {
            return
        }
        if (recordedTime >= 10) {
            Log.d(TAG, "已经有了足够的10S")
            return
        }
        val currentTime = System.currentTimeMillis()
        cacheTime = currentTime

        Log.d(TAG, "startRecording: ")
        val cacheDir: File = context.getExternalCacheDir()?:return
        val finalFileDir = File(cacheDir.absolutePath + File.separator + "recorder")
        if (!finalFileDir.exists()) {
            if (!finalFileDir.mkdirs()) Log.e(TAG, "mkdirs fail path: " + cacheDir.absolutePath)
        }

        val pcmFile = File(finalFileDir, pcmName)
        if (!pcmFile.exists()) {
            pcmFile.createNewFile()
        }
        fileOutPutStream = FileOutputStream(pcmFile, true)
    }
    
    private fun pushPcm(buffer: ByteArray){
        if (fileOutPutStream == null) {
            return
        }
        val currentTime = System.currentTimeMillis()
        val hasRecordTime = (currentTime - cacheTime)/1000 + recordedTime
        Log.d(TAG, "pushPcm: ${hasRecordTime}")
        if (hasRecordTime >= 10) {
            stopPush()
            return
        }

        fileOutPutStream?.write(buffer)
    }
    
    private fun stopPush(){
        if (fileOutPutStream == null) {
            return
        }
        fileOutPutStream?.close()
        fileOutPutStream = null
        val currentTime = System.currentTimeMillis()
        recordedTime = ((currentTime - cacheTime)/1000 + recordedTime).toInt()
        Log.d(TAG, "stopPush: ${recordedTime}")

    }

    private var audioPlayer:PCMAudioPlayer? = null
    
    fun playPcm() {
        val thread = Thread {
            audioPlayer = PCMAudioPlayer()
            audioPlayer!!.initialize()
            audioPlayer!!.startPlayback()
            val cacheDir: File = context.getExternalCacheDir()?:return@Thread
            val finalFileDir = File(cacheDir.absolutePath + File.separator + "recorder")
            val pcmFile = File(finalFileDir, pcmName)

            ////////////////转换
            WavUtils.convertPcmToWav(pcmFile.absolutePath,finalFileDir.absolutePath+File.separator+"out_file.wav",32000,1,16)

            ////////////////

            val inputStream = FileInputStream(pcmFile)
            val buffer = ByteArray(320)
            var length = 0
            while (inputStream.read(buffer).also { length = it } > 0){
                val data = buffer.copyOfRange(0, length)
                audioPlayer!!.writeData(data, 0, length)
            }
            audioPlayer!!.stopPlayback()
            audioPlayer!!.release()
            audioPlayer = null
        }
        thread.start()
    }

    fun deletePcm() {
        val cacheDir: File = context.getExternalCacheDir()?:return
        val finalFileDir = File(cacheDir.absolutePath + File.separator + "recorder")
        val pcmFile = File(finalFileDir, pcmName)
        pcmFile.delete()
    }

    fun uploadAndTalk(){
        queryStatus({ b: Boolean ->
                        if (b) {

                        } else {

                        }
                    })
    }

    private val myVoiceId = "S_NOi0VPxE1"

    private fun queryStatus(listener:(Boolean) -> Unit){
        ApiInstance.queryVoiceTrainStatus(myVoiceId).enqueue(object : Callback<VoiceTrainStatusEntity?> {
            override fun onResponse(call: Call<VoiceTrainStatusEntity?>, response: Response<VoiceTrainStatusEntity?>) {
                if (response.body()?.content?.status == 2 || response.body()?.content?.status == 4) {
                    //代表不需要训练了
                    Toast.makeText(context, "不需要训练了", Toast.LENGTH_SHORT).show()
                    //打开语音
                } else {
                    //还需要训练
                    Toast.makeText(context, "还需要训练", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VoiceTrainStatusEntity?>, t: Throwable) {

            }
        })
    }

    private fun uploadWavFile(){
        val cacheDir: File = context.getExternalCacheDir()?:return
        val finalFileDir = File(cacheDir.absolutePath + File.separator + "recorder")
        val wavFile = File(finalFileDir, "out_file.wav")

        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), wavFile)
        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        val body = MultipartBody.Part.createFormData("audio_bytes", wavFile.name, requestFile)

        ApiInstance.postPcmFile("S_NOi0VPxE1",body).enqueue(object : Callback<CreateRoomEntity> {
            override fun onResponse(call: Call<CreateRoomEntity>, response: Response<CreateRoomEntity>) {
                if (response.body()?.error_code.equals("0000")) {

                } else {
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CreateRoomEntity>, t: Throwable) {
                Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }




    fun speak(content : String){
        speakManager?.addSpeakData(content)
    }

    var speakManager: SpeechEngineTtsLocalManager? = null


    var apiResult = StringBuilder()

    /** 是否收集完毕 **/
    var isCollectFinish = false

    /** 是否要托管播放 **/
    var isAutoSpeak = false


    /**
     * 从孙博处获取到智能体对话的答案，自己将返回的结果播放出来
     */
    fun askQuestionToAI(sessionId: String, question: String) {
        apiResult.clear()
        isCollectFinish = false

        val apiUrl = SseRequest.requestAiSendQuestion(question, sessionId)
        ApiSseRequest.get(apiUrl,false, object : OnSseCallback {
            override fun onSendSuccess() {
                Log.d(TAG,"onSendSuccess")
            }

            override fun onEventSuccess(eventContent: ApiServerEntity?) {
                //此处将返回的结果播放出来，并且将回答扔给智能体
                if(eventContent != null){
                    Log.d(TAG, "onEventSuccess    eventContent   --->${eventContent.message}")
                    apiResult.append(eventContent.message)

                    if(isAutoSpeak){
                        speakManager?.addSpeakData(eventContent.message)
                    }

                    if(eventContent?.isMessage_end_status == true){
                        //收集完毕后
                        isCollectFinish = true
                    }
                }
            }

            override fun onFail(isClickRetry: Boolean) {
                Log.d(TAG,"onFail")
            }

            override fun onOauthVerityFailed() {
                Log.d(TAG,"onOauthVerityFailed")

            }

            override fun onCancel(isClickRetry: Boolean) {
                Log.d(TAG,"onCancel")
            }

            override fun onClose() {
                Log.d(TAG,"onClose")
            }

        })
    }


    // 传入大模型上下文信息
    fun sendLLMMessage(userId: String?, content: String?) {
        val json = JSONObject()
        try {
            json.put("Command", "ExternalTextToLLM")
            json.put("Message", content)
            json.put("InterruptMode", 1) // InterruptMode 可选值1,2,3
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        val jsonString = json.toString()
        val buildBinary = buildBinaryMessage("ctrl", jsonString)
        sendUserBinaryMessage(userId, buildBinary)
    }

    private fun buildBinaryMessage(magic_number: String, content: String): ByteArray {
        val prefixBytes = magic_number.toByteArray(StandardCharsets.UTF_8)
        val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
        val contentLength = contentBytes.size

        val buffer = ByteBuffer.allocate(prefixBytes.size + 4 + contentLength)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.put(prefixBytes)
        buffer.putInt(contentLength)
        buffer.put(contentBytes)
        return buffer.array()
    }

    // userId 为房间内智能体的 ID（对应 StartVoiceChat 中的 AgentConfig.UserId）
    fun sendUserBinaryMessage(userId: String?, buffer: ByteArray?) {
        if (rtcRoom != null) {
            rtcRoom.sendUserBinaryMessage(userId, buffer, MessageConfig.RELIABLE_ORDERED)
        }
    }



}