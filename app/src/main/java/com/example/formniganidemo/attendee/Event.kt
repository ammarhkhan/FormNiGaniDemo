package com.example.formniganidemo.attendee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.formniganidemo.R

@Composable
fun EventCard(
    title: String,
    datetime: String,
    location: String,
    organizer: String,
    price: String,
    imageUri: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = imageUri?.takeIf { it.isNotBlank() },
                    contentDescription = title,
                    placeholder = painterResource(id = R.drawable.central_park_nairobi),
                    error = painterResource(id = R.drawable.central_park_nairobi),
                    fallback = painterResource(id = R.drawable.central_park_nairobi),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color(0xFFE67E22),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = price,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$datetime  |  $location", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D3B36)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = organizer.firstOrNull()?.uppercase() ?: "E"
                        Text(initial, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = organizer, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    eventTitle: String,
    imageUri: String? = null,
    onBackClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3B36)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("GET TICKETS (on M-Tickets)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Header Image Background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(264.dp)
                                .background(Color(0xFF0D3B36))
                        ) {
                            AsyncImage(
                                model = imageUri?.takeIf { it.isNotBlank() },
                                contentDescription = eventTitle,
                                placeholder = painterResource(id = R.drawable.central_park_nairobi),
                                error = painterResource(id = R.drawable.central_park_nairobi),
                                fallback = painterResource(id = R.drawable.central_park_nairobi),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Dark gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.5f),
                                                Color.Transparent,
                                                Color(0xFF0D3B36).copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            )

                            // Back Button
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(start = 16.dp, top = 8.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }

                            // Verified Badge
                            Surface(
                                color = Color(0xFF27AE60),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("VERIFIED EVENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        // 2. Overlapping White Content Sheet
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 240.dp),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                            color = Color(0xFFF8F9FA),
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .width(36.dp)
                                        .height(4.dp)
                                        .background(Color.LightGray, RoundedCornerShape(2.dp))
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = eventTitle, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "📍 Central Park Amphitheater, Nairobi", fontSize = 14.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("DATE", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text("Saturday, 26 Oct 2024", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            }
                                            Column {
                                                Text("TIME", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text("7:00 PM - 1:00 AM", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("Event Description", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "Join us for an unforgettable night of electronic music featuring top DJs from across East Africa! Gates open at 6:30 PM. Licensed bar and food stalls available. 18+ event.",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    lineHeight = 20.sp
                                                )                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("Lineup Highlights", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "DJ Khaligraph, Nviiri The Storyteller, Bensoul, Fena Gitu, Ayrosh",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    lineHeight = 20.sp
                                                )                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}