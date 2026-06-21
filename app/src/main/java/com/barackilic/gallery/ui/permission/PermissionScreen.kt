package com.barackilic.gallery.ui.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.barackilic.gallery.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onGranted: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val permissions = remember { mediaPermissions() }
    val state = rememberMultiplePermissionsState(permissions)
    var hasRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.allPermissionsGranted) {
        if (state.allPermissionsGranted) onGranted()
    }

    val showRationale = state.permissions.any { it.status.shouldShowRationale }
    val permanentlyDenied = hasRequested && !showRationale
    val context = LocalContext.current

    PermissionScreenContent(
        modifier = modifier,
        showRationale = showRationale,
        permanentlyDenied = permanentlyDenied,
        onPrimaryAction = {
            if (permanentlyDenied) {
                context.openAppSettings()
            } else {
                hasRequested = true
                state.launchMultiplePermissionRequest()
            }
        },
        onSkip = onSkip,
    )
}

@Composable
private fun PermissionScreenContent(
    modifier: Modifier,
    showRationale: Boolean,
    permanentlyDenied: Boolean,
    onPrimaryAction: () -> Unit,
    onSkip: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIcon(modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.permission_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))
            val descriptionRes = when {
                permanentlyDenied -> R.string.permission_screen_denied_permanently
                showRationale -> R.string.permission_screen_rationale
                else -> R.string.permission_screen_description
            }
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                val labelRes = if (permanentlyDenied) {
                    R.string.permission_open_settings
                } else {
                    R.string.permission_grant
                }
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(R.string.permission_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Composable equivalent of the launcher adaptive icon: background color + foreground
// webp, clipped to a rounded square. The 1.5x scale compensates for the adaptive icon
// safe zone (108dp canvas, 72dp safe inner area) so the foreground artwork fills the
// visible mask the way the system launcher renders it.
@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.ic_launcher_background)),
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.5f),
        )
    }
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
