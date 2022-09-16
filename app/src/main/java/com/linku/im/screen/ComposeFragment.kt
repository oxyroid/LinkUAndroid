package com.linku.im.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.im.databinding.FragmentComposeBinding
import com.linku.im.ui.theme.AppTheme
import com.linku.im.vm

open class ComposeFragment : Fragment() {
    private var _binding: FragmentComposeBinding? = null
    val binding: FragmentComposeBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    protected fun setContent(content: @Composable () -> Unit) {
        binding.root.setContent {
            AppTheme(
                useDarkTheme = vm.readable.isDarkMode
            ) {
                val systemUiController = rememberSystemUiController()
                LaunchedEffect(vm.readable.isDarkMode) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !vm.readable.isDarkMode
                    )
                }
                content()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}