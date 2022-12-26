@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
)

package ca.amandeep.nycairportsecuritylinewaits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.NYCAirportSecurityLineWaitsTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NYCAirportSecurityLineWaitsTheme { MainScreen(mainViewModel) } }
    }

}

@Composable
@Preview
private fun MainScreen(mainViewModel: MainViewModel = MainViewModel()) {
    val title: MutableState<AirportCode?> = remember { mutableStateOf(null) }
    val navController = rememberAnimatedNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Crossfade(targetState = title.value) { titleValue ->
                        Column {
                            Text(titleValue?.shortCode ?: stringResource(id = R.string.app_name))
                            Text(titleValue?.fullName.orEmpty(), fontSize = 13.sp)
                        }
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.animateContentSize()) {
                        if (title.value == null)
                            Unit
                        else IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                })
        },
    ) { innerPadding ->
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Icon(
                modifier = Modifier
                    .size(130.dp)
                    .alpha(0.15f),
                painter = painterResource(id = R.drawable.statue_of_liberty),
                contentDescription = null
            )
        }
        AnimatedNavHost(navController = navController, startDestination = "selection") {
            slideAnimatedComposable("selection") {
                LaunchedEffect(Unit) {
                    title.value = null
                }
                Selection(innerPadding = innerPadding) {
                    navController.navigate(it)
                }
            }
            AirportCode.values().forEach { airportCode ->
                slideAnimatedComposable(airportCode.shortCode) {
                    LaunchedEffect(Unit) {
                        title.value = airportCode
                        mainViewModel.load(airportCode)
                    }

                    val (refreshing, setRefreshing) = remember { mutableStateOf(false) }
                    LaunchedEffect(refreshing) {
                        if (refreshing) {
                            val jobs = mutableListOf<Job>()
                            jobs += async { delay(300) }
                            jobs += mainViewModel.load(airportCode)

                            jobs.forEach { it.join() }
                            setRefreshing(false)
                        }
                    }

                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing = refreshing),
                        onRefresh = { setRefreshing(true) },
                    ) {
                        Airport(
                            mainViewModel.airports.getOrDefault(
                                airportCode,
                                MainViewModel.Airport()
                            ),
                            innerPadding = innerPadding
                        )
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.slideAnimatedComposable(
    route: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    val animationSpec = tween<IntOffset>(400)
    composable(
        route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = animationSpec
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = animationSpec
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = animationSpec
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = animationSpec
            )
        },
        content = content
    )
}

private const val REMOVE_SWF = true

@Composable
fun Selection(
    innerPadding: PaddingValues,
    navigateTo: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current) + 20.dp,
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current) + 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(40.dp, BiasAlignment.Vertical(-0.4f))
    ) {
        AirportCode.values().toList().let {
            if (REMOVE_SWF)
                it - AirportCode.SWF
            else it
        }.forEach {
            Column(
                Modifier.clickable { navigateTo(it.shortCode) }
            ) {
                Text(
                    it.shortName,
                    fontWeight = FontWeight.Black,
                    fontSize = 60.sp,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    it.fullName
                )
            }
        }
    }
}

