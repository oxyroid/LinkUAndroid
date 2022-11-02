package com.linku.domain.service.system

import com.linku.domain.entity.Message

interface NotificationService {
    fun onCollected(message: Message)
    fun onEmit()
}