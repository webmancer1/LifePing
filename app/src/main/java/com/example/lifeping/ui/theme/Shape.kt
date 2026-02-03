package com.example.lifeping.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Buttons, inputs
    medium = RoundedCornerShape(16.dp), // Cards
    large = RoundedCornerShape(24.dp),  // Dialogs, specialized containers
    extraLarge = RoundedCornerShape(32.dp) // Bottom sheets
)
