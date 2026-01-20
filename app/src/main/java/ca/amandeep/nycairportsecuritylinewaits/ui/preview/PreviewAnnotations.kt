package ca.amandeep.nycairportsecuritylinewaits.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

private const val PIXEL_9_DEVICE = "spec:shape=Normal,width=1080,height=2424,unit=px,dpi=420"

@Preview(
    name = "Pixel_9_Day",
    device = PIXEL_9_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Pixel_9_Night",
    device = PIXEL_9_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class Pixel9DayNightPreview
