package com.example.movilexplora.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.movilexplora.R
import com.example.movilexplora.features.onboarding.PermissionType
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@Composable
fun OnboardingPermissionDialog(
    permissionType: PermissionType,
    onChoiceMade: (always: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val (title, desc, icon) = when (permissionType) {
        PermissionType.LOCATION -> Triple(
            stringResource(R.string.permission_location_title),
            stringResource(R.string.permission_location_desc),
            Icons.Default.LocationOn
        )
        PermissionType.CAMERA -> Triple(
            stringResource(R.string.permission_camera_title),
            stringResource(R.string.permission_camera_desc),
            Icons.Default.CameraAlt
        )
        PermissionType.GALLERY -> Triple(
            stringResource(R.string.permission_gallery_title),
            stringResource(R.string.permission_gallery_desc),
            Icons.Default.PhotoLibrary
        )
        PermissionType.NOTIFICATIONS -> Triple(
            stringResource(R.string.permission_notifications_title),
            stringResource(R.string.permission_notifications_desc),
            Icons.Default.Notifications
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Turquoise,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = desc,
                    fontSize = 14.sp,
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onChoiceMade(true) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                ) {
                    Text(stringResource(R.string.permission_always), color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { onChoiceMade(false) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Turquoise)
                ) {
                    Text(stringResource(R.string.permission_only_once), color = Turquoise, fontWeight = FontWeight.Bold)
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.permission_not_now), color = GrayText)
                }
            }
        }
    }
}
