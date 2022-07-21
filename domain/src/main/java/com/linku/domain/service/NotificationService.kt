package com.linku.domain.service

import com.linku.domain.entity.Message

interface NotificationService {
    fun onReceived(message: Message)
    fun onEmit()
}