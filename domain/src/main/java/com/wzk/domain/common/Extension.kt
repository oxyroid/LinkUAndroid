package com.wzk.domain.common

import com.google.gson.Gson

fun <T> T.toJson() = Gson().toJson(this)