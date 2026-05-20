package com.example.movilexplora.core.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.movilexplora.features.onboarding.PermissionType
import com.example.movilexplora.ui.theme.Turquoise

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R

@Composable
fun OnboardingPermissionDialog(
    permissionType: PermissionType,
    onChoiceMade: (always: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val (title, description) = when (permissionType) {
        PermissionType.LOCATION -> Pair(
            stringResource(R.string.permission_location_title),
            stringResource(R.string.permission_location_desc)
        )
        PermissionType.CAMERA -> Pair(
            stringResource(R.string.permission_camera_title),
            stringResource(R.string.permission_camera_desc)
        )
        PermissionType.GALLERY -> Pair(
            stringResource(R.string.permission_gallery_title),
            stringResource(R.string.permission_gallery_desc)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { onChoiceMade(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                ) {
                    Text(stringResource(R.string.permission_always), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { onChoiceMade(false) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Turquoise)
                ) {
                    Text(stringResource(R.string.permission_only_once), fontWeight = FontWeight.Bold)
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.permission_not_now), color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
