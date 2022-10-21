package com.linku.fs_android

import android.content.Context
import com.linku.fs_android.crypto.AndroidCryptoManager
import com.linku.fs_android.fs.ReadFileSchemeImpl
import com.linku.fs_android.fs.WriteFileSchemeImpl
import com.linku.fs_android.util.Resolver
import com.linku.fs_core.crypto.CryptoManager
import com.linku.fs_core.fs.ReadFileScheme
import com.linku.fs_core.fs.WriteFileScheme
import com.linku.fs_core.logger.Logger

private val logger: Logger
    get() = AndroidLogger

private val Context.resolver: Resolver
    get() = Resolver(applicationContext)

private val cryptoManager: CryptoManager by lazy(
    LazyThreadSafetyMode.SYNCHRONIZED,
    ::AndroidCryptoManager
)

val Context.readFs: ReadFileScheme
    get() = ReadFileSchemeImpl(
        context = applicationContext,
        logger = logger,
        cryptoManager = cryptoManager
    )


val Context.writeFs: WriteFileScheme
    get() = WriteFileSchemeImpl(
        context = applicationContext,
        logger = logger,
        resolver = resolver,
        cryptoManager = cryptoManager
    )

