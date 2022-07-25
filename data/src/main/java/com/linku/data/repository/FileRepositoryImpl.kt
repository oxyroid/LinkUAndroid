package com.linku.data.repository

import android.content.Context
import android.net.Uri
import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.repository.FileRepository
import com.linku.domain.resourceFlow
import com.linku.domain.service.FileService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import java.io.FileNotFoundException
import java.util.*

class FileRepositoryImpl(
    private val context: Context,
    private val fileService: FileService
) : FileRepository {
    override fun upload(uri: Uri?): Flow<Resource<Unit>> = resourceFlow {
        if (uri == null) {
            emitResource("Failed to access file.")
            return@resourceFlow
        }
        val resolver = context.contentResolver
        try {
            resolver.openInputStream(uri).use { stream ->
                if (stream != null) {
                    val bytes = stream.readBytes()
                    val multipart = MultipartBody.create(MediaType.parse("image"), bytes)
                    val filename = UUID.randomUUID().toString()
                    val part = MultipartBody.Part.createFormData("image", filename, multipart)
                    fileService.upload(part)
                        .handleUnit(::emitResource)
                        .catch(::emitResource)
                } else {
                    emitResource("Failed to access file.")
                    return@resourceFlow
                }
            }
        } catch (e: FileNotFoundException) {
            emitResource("File not found.")
            return@resourceFlow
        }
    }
}