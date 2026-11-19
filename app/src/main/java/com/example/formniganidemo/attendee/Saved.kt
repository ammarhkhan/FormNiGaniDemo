package com.example.formniganidemo.attendee

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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import com.example.formniganidemo.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

data class SavedEvent(
    val id: String,
    val title: String,
    val datetime: String,
    val location: String,
    val price: String,
    val imageRes: Int,
    val latLng: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedEventScreen(
    onEventClick: (String) -> Unit
) {
    // Nairobi center coordinates
    val nairobiCenter = LatLng(-1.286389, 36.817223)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nairobiCenter, 13f)
    }

    val savedEvents = remember {
        mutableStateListOf(
            SavedEvent("1", "Live Music: Sundown Sessions", "Sat, Oct 26", "KICC Grounds", "FREE", R.drawable.central_park_nairobi, LatLng(-1.2884, 36.8233)),
            SavedEvent("2", "Tech Innovators Summit", "Wed, Oct 30", "Nairobi Garage", "KES 2,000", R.drawable.central_park_nairobi, LatLng(-1.2657, 36.8026)),
            SavedEvent("3", "Art & Wine Festival", "Sat, Nov 02", "Ngong Racecourse", "KES 1,500", R.drawable.central_park_nairobi, LatLng(-1.3105, 36.7356)),
            SavedEvent("4", "Nairobi Food Market", "Sun, Nov 03", "Alchemist Bar", "FREE", R.drawable.central_park_nairobi, LatLng(-1.2618, 36.8085))
        )
    }

    // Configure the draggable bottom sheet state
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true // Prevents the user from accidentally swiping the drawer completely off-screen
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp, // Height of the drawer when collapsed so the map is clearly visible
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = Color(0xFFF8F9FA),
        sheetShadowElevation = 16.dp,
        sheetDragHandle = {
            // Built-in Material 3 drag handle styled to match your theme
            BottomSheetDefaults.DragHandle(color = Color.LightGray)
        },
        sheetContent = {
            // 2-Column Grid of Saved Events inside the draggable drawer
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(540.dp), // Max expanded height so it doesn't cover the top header completely
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 4.dp),
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
                            // Image banner with bookmark overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = event.imageRes),
                                    contentDescription = event.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { savedEvents.remove(event) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Remove",
                                        tint = Color(0xFF0D3B36),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Details
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${event.datetime} · ${event.price}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.location,
                                    fontSize = 11.sp,
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
    ) {
        // Background content behind the sheet (Map + Floating Header)
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Interactive Map View
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // Pushes Google logo & compass up so they aren't hidden behind the collapsed drawer
                contentPadding = PaddingValues(top = 130.dp, bottom = 165.dp)
            ) {
                savedEvents.forEach { event ->
                    Marker(
                        state = MarkerState(position = event.latLng),
                        title = event.title,
                        snippet = "${event.price} · ${event.datetime}"
                    )
                }
            }

            // 2. Floating Wishlist Header Title
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 64.dp,
                        bottom = 20.dp
                    )
                ) {
                    Text(
                        text = "Saved",
                        color = Color(0xFF0D3B36),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${savedEvents.size} saved events in Nairobi",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}