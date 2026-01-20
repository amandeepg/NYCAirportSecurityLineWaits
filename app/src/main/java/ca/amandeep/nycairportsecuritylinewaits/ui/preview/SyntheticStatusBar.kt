package ca.amandeep.nycairportsecuritylinewaits.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.amandeep.nycairportsecuritylinewaits.R
import ca.amandeep.nycairportsecuritylinewaits.ui.utils.SingleLineHeightStyle

@Composable
internal fun SyntheticStatusBar(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(SyntheticStatusBarHeight)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = StatusBarHorizontalPadding,
                        vertical = StatusBarVerticalPadding
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = PREVIEW_TIME,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeightStyle = SingleLineHeightStyle,
                    ),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(StatusBarIconSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.status_cell_4_bar),
                        contentDescription = null,
                        modifier = Modifier.size(StatusBarIconSize),
                    )
                    Icon(
                        painter = painterResource(R.drawable.status_wifi),
                        contentDescription = null,
                        modifier = Modifier.size(StatusBarIconSize),
                    )
                    Icon(
                        painter = painterResource(R.drawable.status_battery),
                        contentDescription = null,
                        modifier = Modifier.size(StatusBarIconSize),
                    )
                }
            }
        }
    }
}

private const val PREVIEW_TIME = "9:06"
internal val SyntheticStatusBarHeight: Dp = 45.dp
private val StatusBarHorizontalPadding = 25.dp
private val StatusBarVerticalPadding = 6.dp
private val StatusBarIconSpacing = 6.dp
private val StatusBarIconSize: Dp = 16.dp
