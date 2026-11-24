package com.demo.formnigani.serviceprovider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 3. SERVICE PROVIDER VIEW (Blank for now)
@Composable
fun ServiceProviderMainScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(color = Color(0xFF0D3B36)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Service Provider Dashboard (sp1)", color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Service Provider Dashboard (Blank for now)", fontSize = 16.sp, color = Color.Gray)
        }
    }
}