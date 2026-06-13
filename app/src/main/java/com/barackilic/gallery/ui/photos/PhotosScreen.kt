package com.barackilic.gallery.ui.photos

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.barackilic.gallery.data.mediastore.MediaStoreSource
import com.barackilic.gallery.ui.common.PermissionGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PhotosScreen(modifier: Modifier = Modifier) {
    PermissionGate(modifier = modifier) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val count = withContext(Dispatchers.IO) {
                MediaStoreSource(context.contentResolver).count()
            }
            Log.i("Gallery", "Media count: $count")
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Photos — permission granted, see logcat for count")
        }
    }
}
