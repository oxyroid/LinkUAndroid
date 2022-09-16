package com.linku.im.screen.edit

import android.os.Bundle
import android.view.View
import com.linku.im.screen.ComposeFragment
import com.linku.im.screen.introduce.IntroduceEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditFragment : ComposeFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val type = arguments?.getInt("type") ?: -1
        super.onViewCreated(view, savedInstanceState)
        setContent {
            EditScreen(type = IntroduceEvent.Edit.Type.parse(type))
        }
    }
}