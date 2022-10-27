package com.linku.core.fs.impl

import android.content.Context
import com.linku.core.fs.impl.crypto.AndroidCryptoManager
import com.linku.core.fs.impl.fs.ReadFileSchemeImpl
import com.linku.core.fs.impl.fs.WriteFileSchemeImpl
import com.linku.core.fs.impl.util.Resolver

private val logger: com.linku.core.fs.logger.Logger
    get() = AndroidLogger

private val Context.resolver: Resolver
    get() = Resolver(applicationContext)

private val cryptoManager: com.linku.core.fs.crypto.CryptoManager by lazy(
    LazyThreadSafetyMode.SYNCHRONIZED,
    ::AndroidCryptoManager
)

val Context.readFs: com.linku.core.fs.fs.ReadFileScheme
    get() = ReadFileSchemeImpl(
        context = applicationContext,
        logger = logger,
        cryptoManager = cryptoManager
    )


val Context.writeFs: com.linku.core.fs.fs.WriteFileScheme
    get() = WriteFileSchemeImpl(
        context = applicationContext,
        logger = logger,
        resolver = resolver,
        cryptoManager = cryptoManager
    )

