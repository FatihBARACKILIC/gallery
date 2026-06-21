package com.barackilic.gallery.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.barackilic.gallery.R
import com.barackilic.gallery.ui.permission.mediaPermissions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

// Defensive inline fallback for cases where the user landed in a content screen
// without media permission (chose "Daha sonra" on PermissionScreen, or revoked the
// permission via system settings while the app was backgrounded). The dedicated
// onboarding/redesign lives in ui/permission/PermissionScreen.kt.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val permissions = remember { mediaPermissions() }
    val state = rememberMultiplePermissionsState(permissions)
    var hasRequested by rememberSaveable { mutableStateOf(false) }

    if (state.allPermissionsGranted) {
        content()
        return
    }

    val showRationale = state.permissions.any { it.status.shouldShowRationale }
    val permanentlyDenied = hasRequested && !showRationale
    val context = LocalContext.current

    InlinePermissionPlaceholder(
        modifier = modifier,
        permanentlyDenied = permanentlyDenied,
        onRequest = {
            hasRequested = true
            state.launchMultiplePermissionRequest()
        },
        onOpenSettings = { context.openAppSettings() },
    )
}

@Composable
private fun InlinePermissionPlaceholder(
    modifier: Modifier,
    permanentlyDenied: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Photo,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_inline_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = if (permanentlyDenied) onOpenSettings else onRequest) {
            val labelRes = if (permanentlyDenied) {
                R.string.permission_open_settings
            } else {
                R.string.permission_inline_action
            }
            Text(stringResource(labelRes))
        }
    }
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
