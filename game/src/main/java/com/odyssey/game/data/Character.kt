package com.odyssey.game.data

import androidx.compose.ui.graphics.Color
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.BloodRed
import com.odyssey.game.ui.theme.SeaBlue
import com.odyssey.game.ui.theme.TyrianPurple

data class Character(
    val id: String,
    val name: String,
    val title: String,
    val accent: Color,
)

val PENELOPE = Character("penelope", "Penelope", "Queen of Ithaca", TyrianPurple)
val TELEMACHUS = Character("telemachus", "Telemachus", "Prince of Ithaca", SeaBlue)
val EUMAEUS = Character("eumaeus", "Eumaeus", "Master of the Swineherds", Bronze)
val MENTOR = Character("mentor", "\"Mentor\"", "An Old Friend of the House", Color(0xFFB8C4D0))
val EUPEITHES = Character("eupeithes", "Eupeithes", "Father of Antinous", BloodRed)
