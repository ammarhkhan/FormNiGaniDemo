package com.example.formniganidemo.planner

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPlanningScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference
    val placesClient = remember { Places.createClient(context) }

    val deepTeal = Color(0xFF0D3B36)
    val categories = remember {
        listOf("Music", "Festivals", "Professional", "Art", "Social", "Sports")
    }

    // Form Field States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var datetime by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    // Location Search States
    var locationInput by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Image Uri State
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun resetForm() {
        title = ""
        description = ""
        datetime = ""
        price = ""
        category = categories.first()
        isCategoryExpanded = false
        locationInput = ""
        predictions = emptyList()
        selectedLatLng = null
        imageUri = null
    }

    // Query Google Places as the user types
    fun searchPlaces(query: String) {
        if (query.length < 2) {
            predictions = emptyList()
            return
        }
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                predictions = emptyList()
                android.util.Log.e("PlacesError", "Autocomplete failed: ${exception.localizedMessage}")
            }
    }

    // Fetch exact Lat/Lng when a user clicks a suggestion
    fun selectPlace(prediction: AutocompletePrediction) {
        locationInput = prediction.getFullText(null).toString()
        predictions = emptyList() // Hide dropdown

        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                selectedLatLng = response.place.latLng
            }
            .addOnFailureListener {
                selectedLatLng = null
            }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Function to handle Upload & Firestore Save
    fun submitEvent(status: String) {
        if (title.isBlank() || locationInput.isBlank()) {
            Toast.makeText(context, "Please enter a title and select a location from the suggestions", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val docRef = db.collection("events").document()
        val eventId = docRef.id

        fun saveToFirestore(imageUrl: String) {
            val eventMap = hashMapOf(
                "id" to eventId,
                "title" to title,
                "description" to description,
                "location" to locationInput,
                "latitude" to (selectedLatLng?.latitude ?: 0.0),
                "longitude" to (selectedLatLng?.longitude ?: 0.0),
                "datetime" to datetime,
                "price" to price,
                "category" to category,
                "imageUrl" to imageUrl,
                "status" to status,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            docRef.set(eventMap)
                .addOnSuccessListener {
                    isLoading = false
                    if (status == "published") {
                        resetForm()
                        Toast.makeText(context, "Event published successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Draft saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        if (imageUri != null) {
            val imageRef = storageRef.child("event_images/$eventId.jpg")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveToFirestore(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    Toast.makeText(context, "Image upload failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } else {
            saveToFirestore("")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Create Event",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = deepTeal
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Image Picker Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Event Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = deepTeal, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to upload event poster/photo", color = deepTeal, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Dropdown Field
        ExposedDropdownMenuBox(
            expanded = isCategoryExpanded,
            onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false }
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            isCategoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Search Location Field with Dropdown Suggestions
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = locationInput,
                onValueChange = { newValue ->
                    locationInput = newValue
                    searchPlaces(newValue)
                },
                label = { Text("Event Location / Venue") },
                placeholder = { Text("Type venue name (e.g., KICC)") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = deepTeal)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (predictions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp)
                    ) {
                        predictions.forEach { prediction ->
                            ListItem(
                                headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                                supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectPlace(prediction) }
                            )
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = datetime,
            onValueChange = { datetime = it },
            label = { Text("Date & Time (e.g., Sat, Oct 26 | 5:00 PM)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price (e.g., FREE or KES 2,000)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { submitEvent("draft") },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Text("Save Draft", color = deepTeal, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { submitEvent("published") },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = deepTeal),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}