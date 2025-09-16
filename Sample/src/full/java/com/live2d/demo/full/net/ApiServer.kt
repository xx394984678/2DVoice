package com.live2d.demo.full.net

import com.live2d.demo.full.entity.CreateRoomEntity
import com.live2d.demo.full.entity.JoinRoom
import com.live2d.demo.full.entity.VoiceTrainStatusEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiServer {
    @POST("tiktok/chat/room/create")
    fun createRoom(@Query("user_id") user_id:String): Call<CreateRoomEntity>

    @POST("tiktok/chat/room/start")
    fun joinRoom(@Query("room_id") roomId: String,@Query("user_id") user_id: String,@Query("voice_type") voice_type: String): Call<JoinRoom>

    @POST("tiktok/chat/voice/train")
    @Multipart
    fun postPcmFile(@Query("voice_id") roomId: String, @Part() image:MultipartBody.Part): Call<CreateRoomEntity>

    @POST("tiktok/chat/room/stop")
    fun stopRoom(): Call<CreateRoomEntity>

    @GET("tiktok/chat/voice/train/status")
    fun queryVoiceTrainStatus(@Query("voice_id") voice_id: String):Call<VoiceTrainStatusEntity>
}