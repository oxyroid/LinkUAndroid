package com.linku.domain.repository

import android.net.Uri
import com.linku.domain.bean.CachedFile
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    /**
     * @param fixedFormat if url fileName's format is null will append this value.
     */
    fun uploadImage(
        uri: Uri?,
        name: String = "file",
        fixedFormat: String? = null
    ): Flow<FileResource>

}

sealed class MimeType(val value: String) {
    object Txt : MimeType("text/plain")
}

sealed class FileResource {
    object Loading : FileResource()
    data class Success(
        val data: CachedFile
    ) : FileResource()

    object NullUriError : FileResource()
    object FileCannotFoundError : FileResource()
    data class OtherError(val message: String?) : FileResource()
}
