package com.linku.im.screen.create

import android.os.Bundle
import android.view.View
import com.linku.im.screen.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateFragment : ComposeFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent {
            CreateScreen()
        }
    }
}