package com.linku.data.usecase

import android.net.Uri
import com.linku.domain.Resource
import com.linku.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class FileUseCases @Inject constructor(
    val upload: UploadUseCase
)

data class UploadUseCase(
    private val repository: FileRepository
) {
    operator fun invoke(uri: Uri?): Flow<Resource<String>> =
        repository.upload(uri)
}
