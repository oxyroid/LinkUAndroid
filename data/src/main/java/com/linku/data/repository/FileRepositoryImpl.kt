package com.linku.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.domain.Resource
import com.linku.domain.emitOldVersionResource
import com.linku.domain.emitResource
import com.linku.domain.repository.FileRepository
import com.linku.domain.resourceFlow
import com.linku.domain.service.FileService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.FileNotFoundException

class FileRepositoryImpl(
    private val context: Context,
    private val fileService: FileService
) : FileRepository {
    override fun upload(uri: Uri?): Flow<Resource<String>> = resourceFlow {
        if (uri == null) {
            debug { Log.e(TAG, "upload: uri is null.") }
            emitOldVersionResource()
            return@resourceFlow
        }
        val resolver = context.contentResolver
        try {
            resolver.openInputStream(uri).use { stream ->
                if (stream != null) {
                    val bytes = stream.readBytes()
                    val filename = "file.png"
                    val part = MultipartBody.Part.createFormData(
                        "file",
                        filename,
                        RequestBody.create(MediaType.parse("image/*"), bytes)
                    )
                    fileService.upload(part)
                        .handle(::emitResource)
                        .catch(::emitResource)
                } else {
                    debug { Log.e(TAG, "upload: cannot open stream.") }
                    emitOldVersionResource()
                    return@resourceFlow
                }
            }
        } catch (e: FileNotFoundException) {
            debug { Log.e(TAG, "upload: cannot find file.") }
            emitOldVersionResource()
            return@resourceFlow
        }
    }
}