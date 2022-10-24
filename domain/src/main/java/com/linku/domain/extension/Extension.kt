package com.linku.domain.extension

import android.graphics.Bitmap

fun <R> Bitmap.use(block: (Bitmap) -> R): R = block(this).also { recycle() }
