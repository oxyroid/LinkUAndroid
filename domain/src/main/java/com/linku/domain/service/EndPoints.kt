package com.linku.domain.service

import io.ktor.http.*

open class HttpEndPoints(
    open val method: HttpMethod,
    open val params: Map<String, String?> = emptyMap(),
    open vararg val path: String = emptyArray()
)