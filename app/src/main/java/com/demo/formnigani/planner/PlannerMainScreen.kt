package com.demo.formnigani.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.demo.formnigani.EmptyPlaceholderScreen
import com.demo.formnigani.attendee.EventDetailScreen
import com.demo.formnigani.planner.EventPlanningScreen
import com.demo.formnigani.attendee.ExploreScreen

// 2. PLANNER VIEW (EventPlanningScreen)
@Composable
fun PlannerMainScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedEventTitle by remember { mutableStateOf<String?>(null) }
    var selectedVendorName by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("Plan", "Vendor Directory", "Profile")

    Scaffold(
        bottomBar = {
            if (selectedEventTitle == null) {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                when (index) {
                                    0 -> Icon(Icons.Default.Search, contentDescription = item)
                                    1 -> Icon(Icons.Default.DateRange, contentDescription = item)
                                    2 -> Icon(Icons.Default.List, contentDescription = item)
                                    3 -> Icon(Icons.Default.Star, contentDescription = item)
                                    4 -> Icon(Icons.Default.Person, contentDescription = item)
                                }
                            },
                            label = { Text(item) },
                            selected = selectedTab == index,
                            onClick = {
                                if (index == 4) {
                                    onLogout() // Logout when clicking profile tab for testing
                                } else {
                                    selectedTab = index
                                    selectedEventTitle = null
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0D3B36),
                                unselectedIconColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedEventTitle != null) {
//                EventDetailScreen(
//                    eventTitle = selectedEventTitle!!,
//                    onBackClick = { selectedEventTitle = null }
//                )
            } else if(selectedVendorName != null) {
                VendorProfileScreen(
                    vendorName = selectedVendorName!!,
                    onBackClick = { selectedVendorName = null }
                )
            } else {
                when (selectedTab) {
                    0 -> EventPlanningScreen()
                    1 -> VendorDirectoryScreen(onVendorClick = { selectedVendorName = it })
                    else -> EmptyPlaceholderScreen(title = tabs[selectedTab])
                }
            }
        }
    }
}