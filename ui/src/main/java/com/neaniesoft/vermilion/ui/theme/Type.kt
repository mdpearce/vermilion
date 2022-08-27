package com.neaniesoft.vermilion.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.neaniesoft.vermilion.ui.R

val OpenSansFamily = FontFamily(
    Font(R.font.opensans_light, FontWeight.Light),
    Font(R.font.opensans_medium, FontWeight.Medium),
    Font(R.font.opensans_regular, FontWeight.Normal),
    Font(R.font.opensans_semibold, FontWeight.SemiBold),
    Font(R.font.opensans_bold, FontWeight.Bold),
    Font(R.font.opensans_extrabold, FontWeight.ExtraBold),
    Font(R.font.opensans_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.opensans_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.opensans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.opensans_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.opensans_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.opensans_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic)
)

// Set of Material typography styles to start with
val Typography = Typography(
    defaultFontFamily = OpenSansFamily,
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)
