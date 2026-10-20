package com.example.js_comics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.component.TopBar
import com.example.js_comics.ui.theme.Js_comicsTheme
import com.example.js_comics.ui.view.ComicsRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Js_comicsTheme {
                Scaffold(topBar = { TopBar() }) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) { ComicsRoute() }
                }
            }
        }
    }
}
