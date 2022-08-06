package com.linku.domain.repository

import android.net.Uri
import com.linku.domain.Resource
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun upload(uri: Uri?): Flow<Resource<String>>
}