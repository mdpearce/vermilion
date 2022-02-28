package com.neaniesoft.vermilion.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFFcc474b)
val PrimaryLight = Color(0xFFff7877)
val PrimaryDark = Color(0xff950b23)

val Secondary = Color(0xff52c1e0)
val SecondaryLight = Color(0xff8af4ff)
val SecondaryDark = Color(0xff0090ae)

val Pink400 = Color(0xFFCC478E)
val Orange900 = Color(0xFFCC8547)
val Green400 = Color(0xFF4BCC47)

// Oceanic scheme
val DeepBlue = Color(0xFF314549)
val DarkBlue = Color(0xFF263238)
val DeepDarkBlue = Color(0xFF212C31)
val AlmostBlack = Color(0xFF0B0F11)
val LightYellow = Color(0xFFFFCB6B)
val LightBlue = Color(0xFF82AAFF)
val LightRed = Color(0xFFF78C6C)
val LightRedVariant = Color(0xFFFF5370)

// Comment depth indicators
val DepthRed = Color(0xFFDA5B37)
val DepthBlue = Color(0xFF3673DD)
val DepthOrange = Color(0xFFDF972D)
val DepthGreen = Color(0xFF4E8F4E)
val DepthPurple = Color(0xFF9040BD)

val DepthsArray = arrayOf(
    DepthRed, DepthBlue, DepthOrange, DepthGreen, DepthPurple
)

fun colorForDepth(depth: Int): Color {
    return DepthsArray[depth % DepthsArray.size]
}
