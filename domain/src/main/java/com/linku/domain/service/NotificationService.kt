package com.linku.domain.service

import com.linku.domain.entity.Message

interface NotificationService {
    fun onCollected(message: Message)
    fun onEmit()
}