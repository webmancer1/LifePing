package com.example.lifeping.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),   // Buttons, inputs
    medium = RoundedCornerShape(20.dp), // Cards
    large = RoundedCornerShape(28.dp),  // Dialogs, specialized containers
    extraLarge = RoundedCornerShape(36.dp) // Bottom sheets
)
