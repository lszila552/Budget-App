package com.odyssey.game.data

import androidx.compose.ui.graphics.Color
import com.odyssey.game.ui.theme.Bronze
import com.odyssey.game.ui.theme.BloodRed
import com.odyssey.game.ui.theme.SeaBlue
import com.odyssey.game.ui.theme.TyrianPurple

// A small silhouette accessory PortraitBust draws beside a character, standing in for real portrait art.
enum class CharacterProp { NONE, LOOM, SPEAR, CROOK, OWL, RAISED_ARM }

data class Character(
    val id: String,
    val name: String,
    val title: String,
    val accent: Color,
    val prop: CharacterProp = CharacterProp.NONE,
)

val PENELOPE = Character("penelope", "Penelope", "Queen of Ithaca", TyrianPurple, CharacterProp.LOOM)
val TELEMACHUS = Character("telemachus", "Telemachus", "Prince of Ithaca", SeaBlue, CharacterProp.SPEAR)
val EUMAEUS = Character("eumaeus", "Eumaeus", "Master of the Swineherds", Bronze, CharacterProp.CROOK)
val MENTOR = Character("mentor", "\"Mentor\"", "An Old Friend of the House", Color(0xFFB8C4D0), CharacterProp.OWL)
val EUPEITHES = Character("eupeithes", "Eupeithes", "Father of Antinous", BloodRed, CharacterProp.RAISED_ARM)
