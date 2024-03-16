package ca.amandeep.nycairportsecuritylinewaits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.InstantAnimationsRule
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.ThumbnailScale
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Selection
import ca.amandeep.nycairportsecuritylinewaits.ui.main.TitleAndBackBar
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.NYCAirportSecurityLineWaitsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val DATA: List<Any> = run {
    @Suppress("ktlint:standard:no-multi-spaces", "ktlint:standard:comment-wrapping")
    listOf(
        /* isDarkMode */            listOf(true, false),
        /* includeNotifs */         listOf(true, false),
        DEVICE_DATA,
    ).cartesianProduct().map {
        val device = it.last() as List<*>

        arrayOf(
            it[0] as Boolean,
            device[0],
            device[1],
            it[1] as Boolean,
        )
    }
}

@RunWith(Parameterized::class)
class MainScreenshotTest(
    private val isDarkMode: Boolean,
    @Suppress("unused") private val deviceName: String,
    private val device: DeviceConfig,
    private val includeNotifs: Boolean,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(
            name = "darkMode={0}," +
                " device={1}," +
                " showNotifs={3}",
        )
        fun data() = DATA
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = device,
        theme = when (isDarkMode) {
            true -> "android:ThemeOverlay.Material.Dark"
            false -> "android:Theme.Material.Light.NoActionBar"
        },
        thumbnailScale = ThumbnailScale.NoScale,
    )

    @get:Rule
    val instantAnimationsRule = InstantAnimationsRule()

    @Test
    fun screenshotMain() {
        paparazzi.snapshot {
            NYCAirportSecurityLineWaitsTheme(
                darkTheme = isDarkMode,
                dynamicColor = false,
            ) {
                Box(
                    Modifier
                        .padding(5.dp)
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Column {
                        StatusBar(includeNotifs = includeNotifs)
                        Scaffold(
                            topBar = {
                                TitleAndBackBar(
                                    titleAirportCode = null,
                                    NavHostController(paparazzi.context),
                                )
                            },
                        ) {
                            Selection(
                                modifier = Modifier.padding(it),
                                navigateTo = {},
                            )
                        }
                    }
                }
            }
        }
    }
}
