package com.linku.core.fs

import android.content.Context
import com.linku.core.fs.core.ReadFileScheme
import com.linku.core.fs.core.ReadFileSchemeImpl
import com.linku.core.fs.core.WriteFileScheme
import com.linku.core.fs.core.WriteFileSchemeImpl
import com.linku.core.fs.crypto.AndroidCryptoManager
import com.linku.core.fs.crypto.CryptoManager
import com.linku.core.fs.logger.Logger
import com.linku.core.fs.util.Resolver

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

