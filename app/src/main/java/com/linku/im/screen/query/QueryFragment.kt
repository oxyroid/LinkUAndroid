package com.linku.im.screen.query

import android.os.Bundle
import android.view.View
import com.linku.im.screen.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QueryFragment : ComposeFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent { QueryScreen() }
    }
}