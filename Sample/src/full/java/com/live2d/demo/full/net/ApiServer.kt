package com.live2d.demo.full.net

import com.live2d.demo.full.entity.CreateRoomEntity
import com.live2d.demo.full.entity.JoinRoom
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServer {
    @POST("tiktok/chat/room/create")
    fun createRoom(@Query("user_id") user_id:String): Call<CreateRoomEntity>

    @POST("tiktok/chat/room/start")
    fun joinRoom(@Query("room_id") roomId: String,@Query("user_id") user_id: String,@Query("voice_type") voice_type: String): Call<JoinRoom>

    @POST("tiktok/chat/room/stop")
    fun stopRoom(): Call<CreateRoomEntity>
}