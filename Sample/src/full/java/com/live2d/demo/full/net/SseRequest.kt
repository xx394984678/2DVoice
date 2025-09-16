package com.live2d.demo.full.net

import ai.guiji.duix.test.net.OkhttpInstance
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * SSE流式相关请求
 */
object SseRequest {


    /** 发送新问题*/
    //chat_type 1：文字，2：语音
    fun requestAiSendQuestion(question: String?, sessionId: String?): String{
        return OkhttpInstance.BASE_URL.toHttpUrlOrNull()?.newBuilder()
            ?.addPathSegment("tiktok")
            ?.addPathSegment("chat")
            ?.addPathSegment("start")
            ?.addQueryParameter("session_id", sessionId ?: "")
            ?.addQueryParameter("question", question ?: "")
            ?.build().toString() ?: ""
    }
//    /** AI润色*/
//    fun requestAiSendAiPolish(): String{
//        return baseUrl?.newBuilder()
//            ?.addPathSegment("assistant")
//            ?.addPathSegment("comment")
//            ?.addPathSegment("ai_polish")
//            ?.build().toString() ?: ""
//    }
//
//   /** AI生成影评*/
//    fun requestAiSendAiGenerate(): String{
//        return baseUrl?.newBuilder()
//            ?.addPathSegment("assistant")
//            ?.addPathSegment("comment")
//            ?.addPathSegment("ai_generate")
//            ?.build().toString() ?: ""
//    }
//
//
//    /**
//     * ai视频总结
//     * @param fileName 文件名称
//     */
//    fun requestAiVideoAnalysis(fileName: String?, fileId: String?): String {
//        return baseUrl?.newBuilder()
//            ?.addPathSegment("intelligentize")
//            ?.addPathSegment("video_analysis_stream")
//            ?.addQueryParameter("word", fileName ?: "")
//            ?.addQueryParameter("file_id", fileId ?: "")
//            ?.build().toString() ?: ""
//    }
//
//    /**
//     * AI生成影评
//     * @param movie_name 电影名
//     * @param plot 剧情演绎
//     * @param action 演员演绎
//     * @param isLike 1: 点赞 0:踩
//     * @param isFavorite 1: 推荐收藏 2:不推荐收藏
//     * @param word_limit 限制字数
//     * @param isRetry chat: 一键生成 retry:重新生成
//     * @param msg_id 重新生成的时候传这个值
//     */
//    fun requestAiFilmReview(movie_name: String?, plot: String?, action: String?, isLike: Int, isFavorite: Int, word_limit: String?): String{
//        return baseUrl?.newBuilder()
//            ?.addPathSegment("intelligentize")
//            ?.addPathSegment("generate_comment_stream")
//            ?.addQueryParameter("movie_name", movie_name ?: "")
//            ?.addQueryParameter("plot", plot ?: "")
//            ?.addQueryParameter("acting", action ?: "")
//            ?.addQueryParameter("like",  isLike.toString())
//            ?.addQueryParameter("favorite", isFavorite.toString())
//            ?.addQueryParameter("word_limit", word_limit ?: "")
//            ?.addQueryParameter("user_action", "chat")
//            ?.build().toString() ?: ""
//    }
//
//    /**
//     * AI生成影评 - 重新生成
//     * @param msg_id 重新生成的时候传这个值
//     */
//    fun requestAiFilmReviewRetry(msg_id: String? = null): String{
//        return baseUrl?.newBuilder()
//            ?.addPathSegment("intelligentize")
//            ?.addPathSegment("generate_comment_stream")
//            ?.addQueryParameter("user_action", "retry")
//            ?.addQueryParameter("msg_id", msg_id ?: "")
//            ?.build().toString() ?: ""
//    }
//
//    /** 请求token*/
//    fun requestToken(){
//        Thread{
//            TokenManager.getTokenFromServer()
//        }.start()
//    }

}