package com.example.js_comics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.component.TopBar
import com.example.js_comics.ui.theme.Js_comicsTheme
import com.example.js_comics.ui.view.ComicsListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Js_comicsTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Scaffold(topBar = { TopBar() },
                        content = { padding ->
                            Box(modifier = Modifier
                                .padding(padding)
                                .background(Color.Black)) {
                                ComicsListScreen()
                            }
                        })
                }
            }
        }
    }
}