package com.linku.domain.repository

import android.net.Uri
import com.linku.domain.bean.CachedFile
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun uploadImage(
        uri: Uri?,
        name: String = "file"
    ): Flow<FileResource>
}

sealed class FileResource {
    object Loading : FileResource()
    data class Success(
        val data: CachedFile
    ) : FileResource()

    object NullUriError : FileResource()
    object FileCannotFoundError : FileResource()
    data class OtherError(val message: String?): FileResource()
}
