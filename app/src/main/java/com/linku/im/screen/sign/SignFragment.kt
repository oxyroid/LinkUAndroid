package com.linku.im.screen.sign

import android.os.Bundle
import android.view.View
import com.linku.im.screen.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignFragment : ComposeFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent { SignScreen() }
    }
}