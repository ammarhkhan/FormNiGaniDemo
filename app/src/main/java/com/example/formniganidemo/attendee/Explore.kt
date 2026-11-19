package com.example.formniganidemo.attendee

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PartyMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.formniganidemo.attendee.EventCard

@Composable
fun ExploreScreen(onEventClick: (String) -> Unit) {
    val categories = listOf("Music", "Festivals", "Professional", "Art", "Social", "Sports")

    val categoryIconMap = mapOf<String, ImageVector>(
        "Music" to Icons.Default.MusicNote,
        "Festivals" to Icons.Default.Festival,
        "Professional" to Icons.Default.Work,
        "Art" to Icons.Default.Brush,
        "Social" to Icons.Default.PartyMode,
        "Sports" to Icons.Default.Sports,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 75.dp, bottom = 10.dp)) {
                Text(
                    text = "Form Ni Gani",
                    color = Color(0xFF0D3B36),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wrapped OutlinedTextField in a Surface to cast a clean shadow elevation
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White,
                        shadowElevation = 6.dp // Casts the elevated shadow
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("What are you looking for?", fontSize = 14.sp, color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Explore Events",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 1.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(75.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    categoryIconMap.getOrDefault(category, Icons.Default.Star),
                                    contentDescription = null,
                                    tint = Color(0xFF0D3B36),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = category, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            item {
                EventCard(
                    title = "Live Music: Sundown Sessions",
                    datetime = "Sat, Oct 26 | 5:00 PM",
                    location = "KICC Grounds",
                    organizer = "Organized by Jamhuri Events",
                    price = "FREE",
                    onClick = { onEventClick("Live Music: Sundown Sessions") }
                )
            }

            item {
                EventCard(
                    title = "Tech Innovators Summit",
                    datetime = "Wed, Oct 30 | 9:00 AM",
                    location = "Nairobi Garage",
                    organizer = "Organized by Metta ✓",
                    price = "KES 2,000",
                    onClick = { onEventClick("Tech Innovators Summit") }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                EventCard(
                    title = "Pickup Basketball",
                    datetime = "Tues, Oct 29 | 12:00 PM",
                    location = "Nairobi Garage",
                    organizer = "Organized by Big Baller Brand ✓",
                    price = "KES 2,000",
                    onClick = { onEventClick("Pickup Basketball") }
                )
            }
        }
    }
}