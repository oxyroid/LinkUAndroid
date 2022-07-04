package com.linku.domain.common

fun buildUrl(baseUrl: String, builder: UrlBuilder.() -> Unit): String {
    val urlBuilder = UrlBuilder(baseUrl)
    builder.invoke(urlBuilder)
    return urlBuilder.build()
}

data class UrlBuilder(private var url: String) {
    init {
        url = url.trim()
        if (url.endsWith('/')) throw RuntimeException("This url shouldn't be close with '/'.")
    }

    fun path(path: Any) {
        url = "$url/$path"
    }

    fun query(key: String, value: Any?) {
        url = when {
            value == null -> return
            url.endsWith('&') || url.endsWith('?') -> "$url$key=$value&"
            else -> "$url?$key=$value&"
        }
    }

    fun build(): String = run {
        if (url.endsWith('&'))
            url.dropLast(1)
        else url
    }

}