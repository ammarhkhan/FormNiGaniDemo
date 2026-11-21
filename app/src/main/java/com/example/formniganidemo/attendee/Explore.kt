package com.example.formniganidemo.attendee

import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PartyMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

data class NearbyPresetLocation(
    val label: String,
    val latLng: LatLng
)

data class ExploreEventItem(
    val id: String = "",
    val title: String = "",
    val imageUri: String = "",
    val datetime: String = "",
    val location: String = "",
    val organizer: String = "",
    val price: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distanceKm: Double? = null
)

private fun calculateDistanceKm(origin: LatLng, targetLat: Double, targetLng: Double): Double {
    val result = FloatArray(1)
    Location.distanceBetween(origin.latitude, origin.longitude, targetLat, targetLng, result)
    return result[0] / 1000.0
}

@Composable
fun ExploreScreen(
    onEventClick: (String) -> Unit,
    selectedLocation: LatLng? = null,
    selectedLocationLabel: String? = null,
    radiusKm: Double = 15.0
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val deepTeal = Color(0xFF0D3B36)

    val categories = listOf("Music", "Festivals", "Professional", "Art", "Social", "Sports")
    val categoryIconMap = mapOf<String, ImageVector>(
        "Music" to Icons.Default.MusicNote,
        "Festivals" to Icons.Default.Festival,
        "Professional" to Icons.Default.Work,
        "Art" to Icons.Default.Brush,
        "Social" to Icons.Default.PartyMode,
        "Sports" to Icons.Default.Sports,
    )

    // Quick area selectors so you can test nearby filtering immediately
    val areaPresets = remember {
        listOf(
            NearbyPresetLocation("Nairobi CBD", LatLng(-1.286389, 36.817223)),
            NearbyPresetLocation("Westlands", LatLng(-1.2647, 36.8031)),
            NearbyPresetLocation("Kilimani", LatLng(-1.2921, 36.7846)),
            NearbyPresetLocation("Karen", LatLng(-1.3197, 36.7085))
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var activeLocation by remember(selectedLocation) { mutableStateOf(selectedLocation) }
    var activeLocationName by remember(selectedLocationLabel) { mutableStateOf(selectedLocationLabel) }
    var activeRadiusKm by remember(radiusKm) { mutableDoubleStateOf(radiusKm) }

    var firestoreEvents by remember { mutableStateOf<List<ExploreEventItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        db.collection("events")
            .whereEqualTo("status", "published")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    errorText = "Could not load events: ${error.localizedMessage}"
                    Log.e("ExploreScreen", "Firestore listen failed", error)
                    return@addSnapshotListener
                }
                errorText = null
                firestoreEvents = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title").orEmpty()
                    if (title.isBlank()) return@mapNotNull null
                    ExploreEventItem(
                        id = doc.getString("id") ?: doc.id,
                        title = title,
                        imageUri = doc.getString("imageUri") ?: doc.getString("imageUrl").orEmpty(),
                        datetime = doc.getString("datetime").orEmpty(),
                        location = doc.getString("location").orEmpty(),
                        organizer = doc.getString("organizer").orEmpty(),
                        price = doc.getString("price").orEmpty(),
                        category = doc.getString("category").orEmpty(),
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0
                    )
                }.orEmpty()
            }
    }

    val filteredEvents = remember(
        firestoreEvents,
        activeLocation,
        activeRadiusKm,
        selectedCategory,
        searchQuery
    ) {
        val origin = activeLocation
        val withDistance = if (origin != null) {
            firestoreEvents
                .filter { it.latitude != 0.0 || it.longitude != 0.0 }
                .map { event ->
                    event.copy(distanceKm = calculateDistanceKm(origin, event.latitude, event.longitude))
                }
                .filter { (it.distanceKm ?: Double.MAX_VALUE) <= activeRadiusKm }
                .sortedBy { it.distanceKm ?: Double.MAX_VALUE }
        } else {
            firestoreEvents
        }

        withDistance.filter { event ->
            val matchesCategory = selectedCategory == null ||
                    event.category.equals(selectedCategory, ignoreCase = true)
            val query = searchQuery.trim()
            val matchesQuery = query.isEmpty() ||
                    event.title.contains(query, ignoreCase = true) ||
                    event.location.contains(query, ignoreCase = true) ||
                    event.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 75.dp, bottom = 12.dp)) {
                Text(
                    text = "Form Ni Gani",
                    color = deepTeal,
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
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "What are you looking for?",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            },
                            singleLine = true,
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

        val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp + navBarPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore Events",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    if (activeLocation != null) {
                        Text(
                            text = "Within ${activeRadiusKm.roundToInt()} km · Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = deepTeal,
                            modifier = Modifier.clickable {
                                activeLocation = null
                                activeLocationName = null
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick nearby location chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(areaPresets) { preset ->
                        val isSelected = activeLocationName == preset.label
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    activeLocation = null
                                    activeLocationName = null
                                } else {
                                    activeLocation = preset.latLng
                                    activeLocationName = preset.label
                                }
                            },
                            label = { Text(preset.label, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = deepTeal,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 1.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) deepTeal else Color.White,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(75.dp)
                                .clickable {
                                    selectedCategory = if (isSelected) null else category
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    categoryIconMap.getOrDefault(category, Icons.Default.Star),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else deepTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = deepTeal)
                    }
                }
            } else if (errorText != null) {
                item {
                    Text(
                        text = errorText.orEmpty(),
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else if (filteredEvents.isEmpty()) {
                item {
                    Text(
                        text = if (activeLocationName != null) {
                            "No events found within ${activeRadiusKm.roundToInt()} km of $activeLocationName."
                        } else {
                            "No matching events found in Firestore."
                        },
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    val locationDisplay = if (event.distanceKm != null) {
                        "${event.location} · ${String.format("%.1f", event.distanceKm)} km"
                    } else {
                        event.location
                    }
                    val organizerDisplay = if (event.organizer.isNotBlank()) {
                        event.organizer
                    } else if (event.category.isNotBlank()) {
                        event.category
                    } else {
                        "Community Event"
                    }

                    EventCard(
                        title = event.title,
                        datetime = event.datetime,
                        location = locationDisplay,
                        organizer = organizerDisplay,
                        price = event.price,
                        imageUri = event.imageUri,
                        onClick = { onEventClick(event.title) }
                    )
                }
            }
        }
    }
}