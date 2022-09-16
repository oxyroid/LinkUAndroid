package com.linku.im.screen.chat

import android.os.Bundle
import android.view.View
import com.linku.im.screen.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : ComposeFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cid = arguments?.getInt("cid") ?: -1
        super.onViewCreated(view, savedInstanceState)
        setContent {
            ChatScreen(cid = cid)
        }
    }
}