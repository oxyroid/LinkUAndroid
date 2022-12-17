package com.linku.core.extension.io

import android.graphics.Bitmap

fun <R> Bitmap.use(block: (Bitmap) -> R): R = block(this).also { recycle() }

