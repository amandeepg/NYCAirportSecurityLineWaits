@file:OptIn(ExperimentalTextApi::class)

package ca.amandeep.nycairportsecuritylinewaits.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ca.amandeep.nycairportsecuritylinewaits.R

private val GoogleSansFlexWeights = listOf(
    FontWeight.Thin,
    FontWeight.ExtraLight,
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
    FontWeight.Black,
)

private fun googleSansFlexFamily(): FontFamily {
    val fonts = GoogleSansFlexWeights.map { weight ->
        Font(
            R.font.google_sans_flex_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    }
    return FontFamily(*fonts.toTypedArray())
}

private val GoogleSansFlex = googleSansFlexFamily()
private val BaseTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = GoogleSansFlex),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = GoogleSansFlex),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = GoogleSansFlex),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = GoogleSansFlex),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = GoogleSansFlex),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = GoogleSansFlex),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = GoogleSansFlex),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = GoogleSansFlex),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = GoogleSansFlex),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = GoogleSansFlex),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = GoogleSansFlex),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = GoogleSansFlex),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = GoogleSansFlex),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = GoogleSansFlex),
)
