package com.example.js_comics.ui.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.component.LoadingBar
import com.example.component.ShowToast
import com.example.js_comics.data.model.ComicModel
import com.example.js_comics.ui.theme.ComicsShapes

@Composable
fun ComicsListScreen(comicsViewModel: ComicsViewModel = hiltViewModel()) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column {
            ComicsContent(vm = comicsViewModel)
        }
    }
}

@Composable
fun ComicsContent(vm: ComicsViewModel) {
    vm.state.value.let { state ->
        when (state) {
            is Loading -> LoadingBar()
            is ComicsListUiStateReady -> state.comics?.let { BindList(it) }
            is ComicsListUiStateError -> state.error?.let { ShowToast(it) }
        }
    }
}


@Composable
fun ListItem(item: ComicModel) {
    Card(
        shape = ComicsShapes.large,
        elevation = 0.dp,
        modifier = Modifier
            .height(150.dp)
            .padding(8.dp)
            .background(Color.Black)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .wrapContentHeight()
        ) {

            ComicImage(item)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(text = item.title, color = Color.White)

            }
        }
    }
    Divider(color = Color.DarkGray, modifier = Modifier.height(0.5.dp))
}

@Composable
fun BindList(list: List<ComicModel>) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
        items(
            items = list,
            itemContent = {
                ListItem(it)
            })
    }
}

@Composable
fun ComicImage(item: ComicModel) {
    Log.d("jean", "imagem url --> ${item.thumbnail.path}.${item.thumbnail.extension}")
    Image(
        painter = rememberAsyncImagePainter("${item.thumbnail.path}.${item.thumbnail.extension}"),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(120.dp)
            .padding(end = 8.dp)
    )
}

