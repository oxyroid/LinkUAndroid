package com.linku.core.extension

import android.graphics.Bitmap

fun <R> Bitmap.use(block: (Bitmap) -> R): R = block(this).also { recycle() }
