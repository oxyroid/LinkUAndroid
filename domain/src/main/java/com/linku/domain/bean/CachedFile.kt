package com.linku.domain.bean

import android.net.Uri

data class CachedFile(
    val localUri: Uri,
    val remoteUrl: String
)