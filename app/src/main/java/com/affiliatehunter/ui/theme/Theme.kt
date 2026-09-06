package com.affiliatehunter.ui.theme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Primary = Color(0xFF3368A0)
val PrimaryMid = Color(0xFF66A3BF)
val Mint = Color(0xFFC8DFDB)
val BgWarm = Color(0xFFF2EFE7)
val Ink = Color(0xFF25324A)
val Muted = Color(0xFF6B7D94)
val Line = Color(0xFFD6E2E0)
val Ok = Color(0xFF2E7D6B)
val Red = Color(0xFFC25A4A)
val Warn = Color(0xFF8C5A18)

private val LightColors = lightColorScheme(
    primary = Primary, onPrimary = Color.White,
    secondary = PrimaryMid, onSecondary = Color.White,
    tertiary = Mint,
    background = BgWarm, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    error = Red
)
private val Shapes = Shapes(
    small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(14.dp), large = RoundedCornerShape(18.dp)
)
@Composable
fun AffiliateHunterTheme(content: @Composable ()->Unit) {
    MaterialTheme(colorScheme = LightColors, shapes = Shapes, content = content)
}
