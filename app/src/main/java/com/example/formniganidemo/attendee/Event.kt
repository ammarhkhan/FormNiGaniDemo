package com.example.formniganidemo.attendee

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
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
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.formniganidemo.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale


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
    event: EventDetailData,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val deepTeal = Color(0xFF0D3B36)

    // Resolve coordinates (use Firestore lat/lng if present, otherwise geocode the address)
    var resolvedLatLng by remember(event.id, event.latitude, event.longitude, event.location) {
        mutableStateOf(
            if (event.latitude != 0.0 || event.longitude != 0.0) {
                LatLng(event.latitude, event.longitude)
            } else {
                null
            }
        )
    }

    LaunchedEffect(event.location, event.latitude, event.longitude) {
        if (resolvedLatLng == null && event.location.isNotBlank()) {
            val geocoded = withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val results = Geocoder(context, Locale.getDefault())
                        .getFromLocationName(event.location, 1)
                    results?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                } catch (_: Exception) {
                    null
                }
            }
            if (geocoded != null) {
                resolvedLatLng = geocoded
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            resolvedLatLng ?: LatLng(-1.286389, 36.817223),
            15f
        )
    }

    LaunchedEffect(resolvedLatLng) {
        resolvedLatLng?.let { target ->
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, 15f))
        }
    }

    fun openInGoogleMaps() {
        val query = Uri.encode(event.location.ifBlank { event.title })
        val geoUri = if (resolvedLatLng != null) {
            Uri.parse("geo:${resolvedLatLng!!.latitude},${resolvedLatLng!!.longitude}?q=$query")
        } else {
            Uri.parse("geo:0,0?q=$query")
        }

        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapsIntent)
        } else {
            // Fallback to browser if Google Maps app isn't installed
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

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
                        colors = ButtonDefaults.buttonColors(containerColor = deepTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val ticketLabel = if (event.price.isNotBlank()) {
                            "GET TICKETS · ${event.price}"
                        } else {
                            "GET TICKETS (on M-Tickets)"
                        }
                        Text(ticketLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // 1. Header Image Background from event.imageUri
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(264.dp)
                                .background(deepTeal)
                        ) {
                            AsyncImage(
                                model = event.imageUri.takeIf { it.isNotBlank() },
                                contentDescription = event.title,
                                placeholder = painterResource(id = R.drawable.central_park_nairobi),
                                error = painterResource(id = R.drawable.central_park_nairobi),
                                fallback = painterResource(id = R.drawable.central_park_nairobi),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.5f),
                                                Color.Transparent,
                                                deepTeal.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            )

                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(start = 16.dp, top = 8.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }

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
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = event.category.ifBlank { "VERIFIED EVENT" }.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // 2. Overlapping Content Sheet
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

                                Text(
                                    text = event.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 ${event.location.ifBlank { "Location TBA" }}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Clickable Google Map Card
                                if (event.location.isNotBlank() || resolvedLatLng != null) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(1.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(170.dp)
                                            ) {
                                                GoogleMap(
                                                    modifier = Modifier.fillMaxSize(),
                                                    cameraPositionState = cameraPositionState,
                                                    uiSettings = MapUiSettings(
                                                        zoomControlsEnabled = false,
                                                        scrollGesturesEnabled = false,
                                                        zoomGesturesEnabled = false,
                                                        tiltGesturesEnabled = false,
                                                        rotationGesturesEnabled = false,
                                                        mapToolbarEnabled = false
                                                    )
                                                ) {
                                                    resolvedLatLng?.let { pin ->
                                                        Marker(
                                                            state = MarkerState(position = pin),
                                                            title = event.title,
                                                            snippet = event.location
                                                        )
                                                    }
                                                }

                                                // Transparent click overlay so tapping anywhere on the map opens Google Maps
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clickable { openInGoogleMaps() }
                                                )

                                                Surface(
                                                    color = Color.White.copy(alpha = 0.95f),
                                                    shape = RoundedCornerShape(20.dp),
                                                    shadowElevation = 4.dp,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.OpenInNew,
                                                            contentDescription = null,
                                                            tint = deepTeal,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Open in Maps",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = deepTeal
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Date & Price Card
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "DATE & TIME",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = event.datetime.ifBlank { "TBA" },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    "PRICE",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = event.price.ifBlank { "TBA" },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Event Description Card
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "Event Description",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = event.description.ifBlank { "No description provided for this event." },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
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