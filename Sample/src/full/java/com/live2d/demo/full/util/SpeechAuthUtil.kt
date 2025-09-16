package com.live2d.demo.full.util


object SpeechAuthUtil {
    // 火山引擎   Token
    var jwtToken :String = "Jwt;eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiNzYzNDAwMjkwNyJdLCJleHAiOjE3NTgwNTE0NzN9.q1h11xn3SGJQAWAFkYGKhT0_jbk-qeHvS2KGkGGCxdI"
    // 火山引擎   appId
    var appId :String = "7634002907"

    var mSpeechSpeedInt: Int = 10
    var mSpeechVolumeInt: Int = 10
    var mSpeechPitchInt: Int = 10

    // 火山引擎  语音合成  Cluster
    fun getTTSCluster(): String {
        return "volcano_tts"
    }
    // 火山引擎  语音识别  Cluster
    fun getSpeechCluster(): String {
        return "volcengine_streaming_common"
    }
}