package com.linku.domain.service

import com.linku.domain.Result
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {
    @Multipart
    @POST("file/upload")
    suspend fun upload(
        @Part file: MultipartBody.Part
    ): Result<String>
}