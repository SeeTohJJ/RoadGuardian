package com.example.test.View.Elements

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier


// Dropdown menu for hazard type selection in HazardReportingPage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazardTypeDropdown(modifier: Modifier = Modifier, onTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val hazardTypes = listOf("Pothole", "Flooded Road", "Debris", "Accident", "Other")  // Add more options as needed
    var selectedType by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // The text field
        OutlinedTextField(
            value = selectedType,
            onValueChange = { selectedType = it },
            label = { Text("Type of Hazard") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,  // Prevent manual typing
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        // The dropdown menu
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            hazardTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        selectedType = type
                        expanded = false
                        onTypeSelected(type)
                    }
                )
            }
        }
    }
}
