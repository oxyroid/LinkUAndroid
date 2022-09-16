package com.linku.im.screen.introduce

import android.os.Bundle
import android.view.View
import com.linku.im.screen.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroduceFragment : ComposeFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = arguments?.getInt("uid") ?: -1
        super.onViewCreated(view, savedInstanceState)
        setContent {
            IntroduceScreen(uid = uid)
        }
    }
}