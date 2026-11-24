package com.demo.formnigani.attendee

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demo.formnigani.R

data class PastEvent(
    val id: String,
    val title: String,
    val datetime: String,
    val location: String,
    val price: String,
    val imageRes: Int
)

@Composable
fun HistoryScreen(
    onEventClick: (String) -> Unit
) {
    // Sample state representing bookmarked events
    val savedEvents = remember {
        mutableStateListOf(
            PastEvent("1", "Live Music: Sundown Sessions", "Sat, Oct 26", "KICC Grounds", "FREE", R.drawable.central_park_nairobi),
            PastEvent("2", "Tech Innovators Summit", "Wed, Oct 30", "Nairobi Garage", "KES 2,000", R.drawable.central_park_nairobi),
            PastEvent("3", "Art & Wine Festival", "Sat, Nov 02", "Ngong Racecourse", "KES 1,500", R.drawable.central_park_nairobi),
            PastEvent("4", "Nairobi Food Market", "Sun, Nov 03", "Alchemist Bar", "FREE", R.drawable.central_park_nairobi)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Airbnb-style large title header
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 70.dp, bottom = 16.dp)) {
                Text(
                    text = "History",
                    color = Color(0xFF0D3B36),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        }

        // 2-Column Grid matching Airbnb's layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(savedEvents, key = { it.id }) { event ->
                Card(
                    onClick = { onEventClick(event.title) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Image banner with floating bookmark overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Image(
                                painter = painterResource(id = event.imageRes),
                                contentDescription = event.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Floating Bookmark Toggle Button
                            IconButton(
                                onClick = { savedEvents.remove(event) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Remove",
                                    tint = Color(0xFF0D3B36),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Card Text Details
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${event.datetime} · ${event.price}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = event.location,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}