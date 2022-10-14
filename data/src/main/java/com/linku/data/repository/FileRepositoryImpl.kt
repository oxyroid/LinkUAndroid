package com.linku.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.linku.domain.bean.CachedFile
import com.linku.domain.repository.FileRepository
import com.linku.domain.repository.FileResource
import com.linku.domain.resultOf
import com.linku.domain.service.FileService
import com.linku.fs_android.writeFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileService: FileService
) : FileRepository {
    override fun uploadImage(
        uri: Uri?,
        name: String
    ): Flow<FileResource> = flow {
        emit(FileResource.Loading)
        if (uri == null) {
            emit(FileResource.NullUriError)
            return@flow
        }
        val file = context.writeFs.put(uri)
        file ?: run {
            emit(FileResource.FileCannotFoundError)
            return@flow
        }

        val part = MultipartBody.Part.createFormData(
            name,
            file.name,
            RequestBody.create(MediaType.parse("image"), file)
        )
        resultOf { fileService.upload(part) }
            .onSuccess {
                val cachedFile = CachedFile(file.toUri(), it)
                emit(FileResource.Success(cachedFile))
            }
            .onFailure {
                emit(FileResource.OtherError(it.message))
            }
    }
}
