package com.wzk.oss.screen.main

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wzk.oss.extension.toggle
import com.wzk.oss.ui.MaterialTopBar

@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    toggleTheme: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            MaterialTopBar {
                scaffoldState.drawerState.toggle(scope)
            }
        },
        scaffoldState = scaffoldState
    ) {
        LazyColumn {

        }
    }
}