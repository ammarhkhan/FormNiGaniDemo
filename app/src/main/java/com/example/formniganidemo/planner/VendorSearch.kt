package com.example.formniganidemo.planner

import androidx.compose.runtime.Composable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// VENDOR DIRECTORY SCREEN (Clean UI)
@Composable
fun VendorDirectoryScreen(onVendorClick: (String) -> Unit) {
    val categories = listOf("All Services", "Venues", "Catering", "Decor", "Photography")
    val vendors = listOf(
        VendorModel("Soma Decorators", "Event Decor & Styling", "4.8 ★", "112 Reviews", "Dar es Salaam"),
        VendorModel("Karibu Catering", "African & Fusion Cuisine", "4.9 ★", "95 Reviews", "Dar es Salaam"),
        VendorModel("Mizani AV Services", "Audio Visual & Lighting", "4.6 ★", "84 Reviews", "Dar es Salaam")
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Surface(color = Color(0xFF0D3B36), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Vendor Directory", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "Explore trusted services in your city", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEachIndexed { index, cat ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (index == 0) Color.White else Color(0xFF14534D)
                        ) {
                            Text(
                                text = cat,
                                color = if (index == 0) Color(0xFF0D3B36) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(vendors) { vendor ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onVendorClick(vendor.name) }
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.LightGradeOrGray()))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = vendor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Surface(color = Color(0xFF27AE60), shape = RoundedCornerShape(12.dp)) {
                                    Text("✓ Verified", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                            Text(text = vendor.category, fontSize = 13.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${vendor.rating} (${vendor.reviews})  •  📍 ${vendor.location}", fontSize = 12.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onVendorClick(vendor.name) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3B36)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Request Quote", fontSize = 13.sp)
                                }
                                OutlinedButton(
                                    onClick = { onVendorClick(vendor.name) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("View Profile", fontSize = 13.sp, color = Color(0xFF0D3B36))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// VENDOR PROFILE & QUOTE SCREEN
@Composable
fun VendorProfileScreen(vendorName: String, onBackClick: () -> Unit) {
    var inquiryMessage by remember { mutableStateOf("") }
    var requestSent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF0D3B36)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(vendorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA)), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF14534D))) {
                    Surface(
                        color = Color(0xFF27AE60),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        Text("VERIFIED BUSINESS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = vendorName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(text = "Professional Event Services  •  4.8 ★ (112 Reviews)", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("About Service", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Specialized event solutions providing high-end setups, customized themes, and reliable execution for weddings and corporate galas.", fontSize = 14.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Request a Quote / Send Inquiry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        placeholder = { Text("Describe your event date, guest count, and requirements...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (requestSent) {
                        Surface(color = Color(0xFFE8F8F5), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("✓ Inquiry successfully sent to $vendorName!", color = Color(0xFF27AE60), fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        Button(
                            onClick = { requestSent = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3B36)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Send Quote Request", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

data class VendorModel(val name: String, val category: String, val rating: String, val reviews: String, val location: String)

// Placeholder helper for color
fun Color.Companion.LightGradeOrGray() = Color(0xFFE2E8F0)