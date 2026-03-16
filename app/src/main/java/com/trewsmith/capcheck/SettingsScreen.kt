package com.trewsmith.capcheck

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onClearHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ListItem(
            headlineContent = { Text("Dark Mode") },
            trailingContent = {
                Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
            }
        )
        HorizontalDivider()
        
        ListItem(
            headlineContent = { Text("Clear Search History") },
            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
            modifier = Modifier.padding(vertical = 8.dp),
            trailingContent = {
                TextButton(onClick = onClearHistory) {
                    Text("Clear")
                }
            }
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text("App Version") },
            supportingContent = { Text(BuildConfig.VERSION_NAME) },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
        )
        HorizontalDivider()

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}
