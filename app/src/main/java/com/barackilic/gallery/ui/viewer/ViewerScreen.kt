package com.barackilic.gallery.ui.viewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.barackilic.gallery.R
import com.barackilic.gallery.data.mediastore.contentUri
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.ui.common.MediaThumbRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.media3.common.MediaItem as Media3MediaItem

@Composable
fun ViewerScreen(
    initialIndex: Int,
    bucketId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ViewerViewModel = koinViewModel { parametersOf(bucketId) }
    val items = viewModel.items.collectAsLazyPagingItems()
    val isFavorite by viewModel.isCurrentFavorite.collectAsState()

    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .build()
            .apply {
                // Muted by default — opening a video shouldn't blast audio.
                // The user toggles via the overlay's mute control.
                volume = 0f
            }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) player.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var systemBarsVisible by rememberSaveable { mutableStateOf(true) }
    ImmersiveSystemBars(visible = systemBarsVisible)

    var currentItem by remember { mutableStateOf<MediaItem?>(null) }
    val isVideoPage = currentItem?.type == MediaType.Video
    val playerSnapshot = rememberPlayerSnapshot(player)

    // Auto-hide: bars reappear via tap or video-page-show, then fade after
    // AUTO_HIDE_MS of no further interaction. While a video is paused we keep
    // bars on screen — standard player UX so the user can find the play button.
    LaunchedEffect(systemBarsVisible, isVideoPage, playerSnapshot.isPlaying) {
        if (!systemBarsVisible) return@LaunchedEffect
        if (isVideoPage && !playerSnapshot.isPlaying) return@LaunchedEffect
        delay(AUTO_HIDE_MS)
        systemBarsVisible = false
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonText = stringResource(R.string.coming_soon)
    val showComingSoon: () -> Unit = {
        scope.launch { snackbarHostState.showSnackbar(comingSoonText) }
    }

    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { /* MediaStore observer drives the refresh; no app state to update here. */ }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ViewerPager(
            items = items,
            initialIndex = initialIndex,
            player = player,
            onToggleBars = { systemBarsVisible = !systemBarsVisible },
            onVideoPageShown = { systemBarsVisible = true },
            onCurrentItemChanged = { item ->
                currentItem = item
                viewModel.setCurrentMediaId(item?.id)
            },
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = systemBarsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            ViewerTopBar(
                onBack = onBack,
                onShare = currentItem?.let { item -> { shareMediaItem(context, item) } },
            )
        }
        // Stacked bottom UI: action bar (always when bars visible), and on video
        // pages the custom video controls overlay sits directly above it.
        // Single scrim wraps both rows — kullanıcı bir bütün olarak görsün.
        AnimatedVisibility(
            visible = systemBarsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = SCRIM_ALPHA)),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    if (isVideoPage) {
                        val videoUri = currentItem?.contentUri()
                        if (videoUri != null) {
                            VideoControlsOverlay(
                                player = player,
                                snapshot = playerSnapshot,
                                mediaUri = videoUri,
                            )
                        }
                    }
                    ViewerBottomBar(
                        isFavorite = isFavorite,
                        onShare = currentItem?.let { item -> { shareMediaItem(context, item) } },
                        onToggleFavorite = currentItem?.let { { viewModel.toggleCurrentFavorite() } },
                        onEdit = showComingSoon,
                        onTrash = currentItem?.let { item ->
                            { trashLauncher.launch(viewModel.buildTrashRequest(item)) }
                        },
                        onInfo = showComingSoon,
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = SNACKBAR_BOTTOM_OFFSET.dp),
        )
    }
}

@Composable
private fun ViewerPager(
    items: LazyPagingItems<MediaItem>,
    initialIndex: Int,
    player: ExoPlayer,
    onToggleBars: () -> Unit,
    onVideoPageShown: () -> Unit,
    onCurrentItemChanged: (MediaItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemCount = items.itemCount
    if (itemCount == 0) {
        Box(modifier = modifier.background(Color.Black))
        return
    }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, itemCount - 1),
    ) { items.itemCount }

    LaunchedEffect(pagerState, player) {
        // itemCount is part of the key so the effect re-runs when the underlying
        // paging list shrinks (e.g. an item was trashed). Without it, page=N may
        // still hold a stale MediaItem after invalidation.
        snapshotFlow { pagerState.currentPage to items.itemCount }
            .distinctUntilChanged()
            .collect { (page, count) ->
                // After trashing the last visible item, currentPage may briefly point
                // past the new end before the pager settles. Guard the peek call.
                val current = if (page in 0 until count) items.peek(page) else null
                onCurrentItemChanged(current)
                if (current?.type == MediaType.Video) {
                    val mediaId = current.id.toString()
                    val isNewVideo = player.currentMediaItem?.mediaId != mediaId
                    if (isNewVideo) {
                        player.setMediaItem(
                            Media3MediaItem.Builder()
                                .setMediaId(mediaId)
                                .setUri(current.contentUri())
                                .build(),
                        )
                        player.prepare()
                        // Auto-play only on first visit. Returning to a video we
                        // already paused (swiped to a photo and back) keeps it
                        // paused — user pressed back here for a reason; resume
                        // is a deliberate tap on PlayerView's play button.
                        player.playWhenReady = true
                    }
                    onVideoPageShown()
                } else {
                    player.pause()
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 1,
        key = { page -> items.peek(page)?.id ?: page },
    ) { page ->
        val item = items[page]
        if (item != null) {
            ViewerPage(
                item = item,
                player = if (page == pagerState.currentPage) player else null,
                onToggleBars = onToggleBars,
            )
        }
    }
}

@Composable
private fun ViewerPage(
    item: MediaItem,
    player: ExoPlayer?,
    onToggleBars: () -> Unit,
) {
    when (item.type) {
        MediaType.Image -> {
            // The grid already loaded a small thumbnail via MediaThumbRequest; reuse it
            // as a placeholder so high-res photos don't show a black flash while
            // Telephoto decodes (or subsamples) the full image.
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = MediaThumbRequest(item.contentUri()),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                ZoomableAsyncImage(
                    model = item.contentUri(),
                    contentDescription = null,
                    onClick = { onToggleBars() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        MediaType.Video -> VideoPage(item = item, player = player)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoPage(
    item: MediaItem,
    player: ExoPlayer?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Thumb poster so adjacent (off-current) video pages show a frame instead of black.
        AsyncImage(
            model = MediaThumbRequest(item.contentUri()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        // Custom Compose overlay replaces ExoPlayer's built-in controller;
                        // see VideoControlsOverlay. PlayerView still handles surface +
                        // aspect — useController stays off.
                        useController = false
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                update = { view -> view.player = player },
                onRelease = { view -> view.player = null },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    onBack: () -> Unit,
    onShare: (() -> Unit)?,
) {
    // Scrim: dark → transparent vertical gradient so white icons stay readable when
    // the photo top is bright (sky, snow). Wraps the bar so the gradient extends
    // through status-bar inset too.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = SCRIM_ALPHA), Color.Transparent),
                ),
            ),
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onShare ?: {},
                    enabled = onShare != null,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = Color.White,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            modifier = Modifier.statusBarsPadding(),
        )
    }
}

@Composable
private fun ViewerBottomBar(
    isFavorite: Boolean,
    onShare: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onEdit: () -> Unit,
    onTrash: (() -> Unit)?,
    onInfo: () -> Unit,
) {
    // Scrim ve navigationBarsPadding üst kapsayıcı Column'da uygulanır — bu Row
    // sadece ikonları layout'lar, böylece video strip ile aynı scrim altında
    // bir bütün gibi durur.
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ViewerActionIcon(
                icon = Icons.Outlined.Share,
                contentDescription = stringResource(R.string.share),
                onClick = onShare,
            )
            ViewerActionIcon(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.viewer_unfavorite else R.string.viewer_favorite,
                ),
                onClick = onToggleFavorite,
            )
            ViewerActionIcon(
                icon = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.viewer_edit),
                onClick = onEdit,
            )
            ViewerActionIcon(
                icon = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.trash_send_to),
                onClick = onTrash,
            )
            ViewerActionIcon(
                icon = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.viewer_info),
                onClick = onInfo,
            )
        }
    }
}

@Composable
private fun ViewerActionIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: (() -> Unit)?,
) {
    IconButton(
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}

private fun shareMediaItem(context: Context, item: MediaItem) {
    val mime = when (item.type) {
        MediaType.Image -> "image/*"
        MediaType.Video -> "video/*"
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, item.contentUri())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, null))
}

@Composable
private fun ImmersiveSystemBars(visible: Boolean) {
    val view = LocalView.current
    LaunchedEffect(visible) {
        val activity = view.context as? Activity ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (visible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(view) {
        onDispose {
            val activity = view.context as? Activity ?: return@onDispose
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

private const val SEEK_INCREMENT_MS = 10_000L
private const val AUTO_HIDE_MS = 4_000L
private const val SCRIM_ALPHA = 0.7f
private const val SNACKBAR_BOTTOM_OFFSET = 96
