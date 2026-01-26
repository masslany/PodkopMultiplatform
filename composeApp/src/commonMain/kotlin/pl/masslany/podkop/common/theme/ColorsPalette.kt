package pl.masslany.podkop.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorsPalette(
    val nameOrange: Color = Color.Unspecified,
    val nameBurgundy: Color = Color.Unspecified,
    val nameGreen: Color = Color.Unspecified,
    val nameBlack: Color = Color.Unspecified,
    val genderBlue: Color = Color.Unspecified,
    val genderPink: Color = Color.Unspecified,
    val genderGray: Color = Color.Unspecified,
    val tagBlue: Color = Color.Unspecified,
    val hotOrange: Color = Color.Unspecified,
    val adultRed: Color = Color.Unspecified,
    val votePositive: Color = Color.Unspecified,
    val voteNegative: Color = Color.Unspecified,
)

val LightNameOrange = Color(color = 0xFFFF5100)
val LightNameBurgundy = Color(color = 0xFFB00000)
val LightNameGreen = Color(color = 0xFF00A63D)
val LightNameBlack = Color(color = 0xFF000000)
val LightGenderBlue = Color(color = 0xFF07B8F4)
val LightGenderPink = Color(color = 0xFFFF3BD4)
val LightGenderGray = Color(color = 0xFFE5E5E5)
val LightTagBlue = Color(color = 0xFF3D83CC)
val LightHotOrange = Color(color = 0xFFEF713F)
val LightAdultRed = Color(color = 0xFFE86064)
val LightVotePositive = Color(color = 0xFF74BD74)
val LightVoteNegative = Color(color = 0xFFE7625A)

val DarkNameOrange = Color(color = 0xFFFE5000)
val DarkNameBurgundy = Color(color = 0xFFD20000)
val DarkNameGreen = Color(color = 0xFF00A33C)
val DarkNameBlack = Color(color = 0xFFFFFFFF)
val DarkGenderBlue = Color(color = 0xFF07B8F4)
val DarkGenderPink = Color(color = 0xFFBf48A7)
val DarkGenderGray = Color(color = 0xFFE5E5E5)
val DarkTagBlue = Color(color = 0xFF3D83CC)
val DarkHotOrange = Color(color = 0xFFEF713F)
val DarkAdultRed = Color(color = 0xFFE86064)
val DarkVotePositive = Color(color = 0xFF74BD74)
val DarkVoteNegative = Color(color = 0xFFE7625A)

val LightColorsPalette = ColorsPalette(
    nameOrange = LightNameOrange,
    nameBurgundy = LightNameBurgundy,
    nameGreen = LightNameGreen,
    nameBlack = LightNameBlack,
    genderBlue = LightGenderBlue,
    genderPink = LightGenderPink,
    genderGray = LightGenderGray,
    tagBlue = LightTagBlue,
    hotOrange = LightHotOrange,
    adultRed = LightAdultRed,
    votePositive = LightVotePositive,
    voteNegative = LightVoteNegative,
)

val DarkColorsPalette = ColorsPalette(
    nameOrange = DarkNameOrange,
    nameBurgundy = DarkNameBurgundy,
    nameGreen = DarkNameGreen,
    nameBlack = DarkNameBlack,
    genderBlue = DarkGenderBlue,
    genderPink = DarkGenderPink,
    genderGray = DarkGenderGray,
    tagBlue = DarkTagBlue,
    hotOrange = DarkHotOrange,
    adultRed = DarkAdultRed,
    votePositive = DarkVotePositive,
    voteNegative = DarkVoteNegative,
)

val LocalColorsPalette = staticCompositionLocalOf { ColorsPalette() }

val MaterialTheme.colorsPalette: ColorsPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalColorsPalette.current
