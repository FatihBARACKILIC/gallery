package com.barackilic.gallery.ui.viewer

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.barackilic.gallery.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

internal data class PlayerSnapshot(
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean,
    val isMuted: Boolean,
)

@Composable
internal fun rememberPlayerSnapshot(player: ExoPlayer): PlayerSnapshot {
    var positionMs by remember(player) { mutableLongStateOf(player.currentPosition.coerceAtLeast(0L)) }
    var durationMs by remember(player) { mutableLongStateOf(player.duration.coerceAtLeast(0L)) }
    var isPlaying by remember(player) { mutableStateOf(player.isPlaying) }
    var isMuted by remember(player) { mutableStateOf(player.volume == 0f) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = player.duration.coerceAtLeast(0L)
                }
            }

            override fun onVolumeChanged(volume: Float) {
                isMuted = volume == 0f
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
            ) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, isPlaying) {
        while (isPlaying) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            durationMs = player.duration.coerceAtLeast(0L)
            delay(POSITION_POLL_MS)
        }
        positionMs = player.currentPosition.coerceAtLeast(0L)
        durationMs = player.duration.coerceAtLeast(0L)
    }

    return PlayerSnapshot(
        positionMs = positionMs,
        durationMs = durationMs,
        isPlaying = isPlaying,
        isMuted = isMuted,
    )
}

// Stacked controls — all under the parent scrim:
//   1. Buttons row: play/pause (center, small) + mute (right, small)
//   2. Timeline:
//      - collapsed: single full-width poster (video frame 0); tap → expand
//      - expanded: 40-frame scrubber strip with fixed center line
//   3. (Parent renders ViewerBottomBar below)
@Composable
internal fun VideoControlsOverlay(
    player: ExoPlayer,
    snapshot: PlayerSnapshot,
    mediaUri: Uri,
    modifier: Modifier = Modifier,
) {
    val appContext = LocalContext.current.applicationContext
    val frameSource = remember(mediaUri) { VideoFrameSource(appContext, mediaUri) }
    DisposableEffect(frameSource) {
        onDispose { frameSource.release() }
    }

    var expanded by remember(mediaUri) { mutableStateOf(false) }
    val safeDuration = snapshot.durationMs.coerceAtLeast(1L)

    var userIsTouching by remember { mutableStateOf(false) }
    var scrubStartMs by remember { mutableLongStateOf(0L) }
    var scrubDeltaMs by remember { mutableLongStateOf(0L) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            if (userIsTouching) {
                Text(
                    text = formatSignedDelta(scrubDeltaMs),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                IconButton(
                    onClick = {
                        if (snapshot.isPlaying) player.pause() else player.play()
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(BUTTON_SIZE_DP.dp),
                ) {
                    Icon(
                        imageVector = if (snapshot.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (snapshot.isPlaying) R.string.video_pause else R.string.video_play,
                        ),
                        tint = Color.White,
                        modifier = Modifier.size(BUTTON_ICON_DP.dp),
                    )
                }
            }
            IconButton(
                onClick = {
                    player.volume = if (snapshot.isMuted) 1f else 0f
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(BUTTON_SIZE_DP.dp),
            ) {
                Icon(
                    imageVector = if (snapshot.isMuted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = stringResource(
                        if (snapshot.isMuted) R.string.video_unmute else R.string.video_mute,
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(BUTTON_ICON_DP.dp),
                )
            }
        }

        if (expanded) {
            FrameStrip(
                player = player,
                snapshot = snapshot,
                frameSource = frameSource,
                safeDuration = safeDuration,
                onScrubStart = { startMs ->
                    scrubStartMs = startMs
                    scrubDeltaMs = 0L
                    userIsTouching = true
                },
                onScrubUpdate = { newMs ->
                    scrubDeltaMs = newMs - scrubStartMs
                },
                onScrubEnd = {
                    userIsTouching = false
                    scrubDeltaMs = 0L
                },
            )
        } else {
            CollapsedPoster(
                frameSource = frameSource,
                onTap = { expanded = true },
            )
        }
    }
}

@Composable
private fun CollapsedPoster(
    frameSource: VideoFrameSource,
    onTap: () -> Unit,
) {
    // Match the expanded strip's center-frame layout: a single 56dp square at
    // the center scrubber line. Tap anywhere on the row expands.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(STRIP_HEIGHT_DP.dp)
            .clickable { onTap() },
    ) {
        FrameImage(
            source = frameSource,
            timeMs = 0L,
            bitmapSizePx = STRIP_THUMB_PX,
            modifier = Modifier
                .align(Alignment.Center)
                .size(THUMB_SIZE_DP.dp),
        )
    }
}

@Composable
private fun FrameStrip(
    player: ExoPlayer,
    snapshot: PlayerSnapshot,
    frameSource: VideoFrameSource,
    safeDuration: Long,
    onScrubStart: (Long) -> Unit,
    onScrubUpdate: (Long) -> Unit,
    onScrubEnd: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var isTouching by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(STRIP_HEIGHT_DP.dp),
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val thumbWidthPx = with(density) { THUMB_SIZE_DP.dp.toPx() }
        // Make the strip's total scroll length proportional to video duration
        // so a 1dp drag ≈ a fixed number of ms regardless of clip length, then
        // derive how many *bitişik* (gapless) thumbs fit. effectiveFrameCount
        // is capped so super-long videos don't spawn thousands of MMR calls.
        val pxPerMs = with(density) { DP_PER_SECOND.dp.toPx() / 1000f }
        val baseStripPx = FRAME_COUNT_MIN * thumbWidthPx
        val targetStripPx = (safeDuration * pxPerMs).coerceAtLeast(baseStripPx)
        val effectiveFrameCount = (targetStripPx / thumbWidthPx)
            .toInt()
            .coerceIn(FRAME_COUNT_MIN, FRAME_COUNT_MAX)
        val totalStripPx = effectiveFrameCount * thumbWidthPx
        val sidePaddingDp = with(density) { ((containerWidthPx - thumbWidthPx) / 2).toDp() }

        var wasPlaying by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isTouching = true
                        // Pause so each seekTo() decodes + renders to the
                        // surface (playback otherwise races against scrubs and
                        // only the last seek shows). EXACT default seek lets
                        // the strip be millisecond-accurate.
                        wasPlaying = player.isPlaying
                        player.pause()
                        onScrubStart(player.currentPosition.coerceAtLeast(0L))
                        do {
                            val ev = awaitPointerEvent()
                            if (ev.changes.none { it.pressed }) break
                        } while (true)
                        isTouching = false
                        if (wasPlaying) player.play()
                        onScrubEnd()
                    }
                },
        ) {
            Spacer(modifier = Modifier.width(sidePaddingDp))
            for (i in 0 until effectiveFrameCount) {
                val timeMs = (i.toLong() * safeDuration) / effectiveFrameCount
                FrameImage(
                    source = frameSource,
                    timeMs = timeMs,
                    bitmapSizePx = STRIP_THUMB_PX,
                    modifier = Modifier.size(THUMB_SIZE_DP.dp),
                )
            }
            Spacer(modifier = Modifier.width(sidePaddingDp))
        }

        // Auto-scroll keeps playhead under the center line while playing. animateScrollTo
        // with linear easing equal to the poll interval = continuous slide instead of
        // discrete jumps.
        LaunchedEffect(snapshot.positionMs, snapshot.isPlaying, isTouching, totalStripPx, safeDuration) {
            if (isTouching || !snapshot.isPlaying || totalStripPx <= 0f) return@LaunchedEffect
            val target = ((snapshot.positionMs.toFloat() / safeDuration) * totalStripPx).roundToInt()
                .coerceIn(0, scrollState.maxValue)
            scrollState.animateScrollTo(
                target,
                animationSpec = tween(durationMillis = POSITION_POLL_MS.toInt(), easing = LinearEasing),
            )
        }

        LaunchedEffect(scrollState, isTouching, safeDuration) {
            if (!isTouching || totalStripPx <= 0f) return@LaunchedEffect
            // conflate() drops intermediate scroll values when the previous
            // seekTo is still decoding — keeps render up with the finger
            // without queueing dozens of seeks per second.
            snapshotFlow { scrollState.value }
                .conflate()
                .collect { value ->
                    val time = (value.toFloat() / totalStripPx * safeDuration).toLong()
                        .coerceIn(0L, safeDuration)
                    onScrubUpdate(time)
                    player.seekTo(time)
                }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.9f)),
        )
    }
}

@Composable
private fun FrameImage(
    source: VideoFrameSource,
    timeMs: Long,
    bitmapSizePx: Int,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(source, timeMs, bitmapSizePx) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(source, timeMs, bitmapSizePx) {
        if (bitmap == null) {
            bitmap = source.frame(timeMs, bitmapSizePx)
        }
    }
    Box(modifier = modifier.background(Color.DarkGray.copy(alpha = 0.6f))) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

private fun formatSignedDelta(deltaMs: Long): String {
    val sign = if (deltaMs >= 0) "+" else "-"
    val abs = deltaMs.absoluteValue
    val totalSec = abs / 1000
    val ms = abs % 1000
    return if (totalSec < 60) {
        String.format(Locale.US, "%s%d.%03ds", sign, totalSec, ms)
    } else {
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        String.format(Locale.US, "%s%d:%02d.%03d", sign, minutes, seconds, ms)
    }
}

private const val POSITION_POLL_MS = 200L
private const val THUMB_SIZE_DP = 56
private const val STRIP_HEIGHT_DP = 56
private const val BUTTON_SIZE_DP = 32
private const val BUTTON_ICON_DP = 18

// Bitmap decode size for both collapsed poster and strip thumbs (56dp ≈ 56-110px
// depending on density; 80px is a safe single-tier).
private const val STRIP_THUMB_PX = 80

// Pixel density along the scroll axis — 200dp per second of video means 1dp of
// drag ≈ 5ms, which is sub-frame at 30/60fps.
private const val DP_PER_SECOND = 200

// Thumb-count bounds. MIN gives short clips enough strip width to feel scrubby;
// MAX caps RAM/MMR work for very long videos. Thumbs are always bitişik —
// effectiveFrameCount = totalStripPx / thumbWidth, clamped to this range.
private const val FRAME_COUNT_MIN = 40
private const val FRAME_COUNT_MAX = 200
