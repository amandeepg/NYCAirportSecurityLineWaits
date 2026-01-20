package ca.amandeep.nycairportsecuritylinewaits.ui.main

import androidx.compose.runtime.Composable
import ca.amandeep.nycairportsecuritylinewaits.ui.preview.MarketingEwrPreview
import ca.amandeep.nycairportsecuritylinewaits.ui.preview.MarketingJfkPreview
import ca.amandeep.nycairportsecuritylinewaits.ui.preview.MarketingLgaPreview
import ca.amandeep.nycairportsecuritylinewaits.ui.preview.MarketingSelectionPreview
import ca.amandeep.nycairportsecuritylinewaits.ui.preview.MarketingSelectionSingleThemePreview
import ca.amandeep.nycairportsecuritylinewaits.ui.preview.Pixel9DayNightPreview
import com.android.tools.screenshot.PreviewTest

@PreviewTest
@Pixel9DayNightPreview
@Composable
fun AirportSelectionScreenshot() = MarketingSelectionPreview()

@PreviewTest
@Pixel9DayNightPreview
@Composable
fun AirportSelectionSingleThemeScreenshot() = MarketingSelectionSingleThemePreview(darkTheme = false)

@PreviewTest
@Pixel9DayNightPreview
@Composable
fun AirportEwrScreenshot() = MarketingEwrPreview()

@PreviewTest
@Pixel9DayNightPreview
@Composable
fun AirportJfkScreenshot() = MarketingJfkPreview()

@PreviewTest
@Pixel9DayNightPreview
@Composable
fun AirportLgaScreenshot() = MarketingLgaPreview()
