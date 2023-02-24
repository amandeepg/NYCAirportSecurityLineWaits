@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)

package ca.amandeep.nycairportsecuritylinewaits.ui.main

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import ca.amandeep.nycairportsecuritylinewaits.R
import ca.amandeep.nycairportsecuritylinewaits.data.AirportCode
import ca.amandeep.nycairportsecuritylinewaits.ui.AirportScreen
import ca.amandeep.nycairportsecuritylinewaits.ui.ErrorScreen
import ca.amandeep.nycairportsecuritylinewaits.util.ConnectionState
import ca.amandeep.nycairportsecuritylinewaits.util.observeConnectivity
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel()
) {
    var titleAirportCode by remember { mutableStateOf<AirportCode?>(null) }
    val navController = rememberAnimatedNavController()
    Scaffold(
        modifier = modifier,
        topBar = { TitleAndBackBar(titleAirportCode, navController) }
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
                // Clear the title when we navigate to the main selection screen
                LaunchedEffect(Unit) {
                    titleAirportCode = null
                }
                Box(Modifier.padding(innerPadding)) {
                    Selection(navigateTo = navController::navigate)
                }
            }
            // Create a details screen for each airport
            AirportCode.values().forEach { airportCode ->
                val uiStateFlow = mainViewModel.getWaitTimes(airportCode)

                slideAnimatedComposable(airportCode.shortCode) {
                    // Set the title to the airport when we navigate to the details screen
                    LaunchedEffect(Unit) {
                        titleAirportCode = airportCode
                    }

                    var refreshing by remember { mutableStateOf(false) }
                    LaunchedEffect(refreshing) {
                        if (refreshing) {
                            val elapsedMillis = measureTimeMillis {
                                mainViewModel.refreshAirportFromNetwork(airportCode)
                            }
                            delay((500 - elapsedMillis).milliseconds)
                            refreshing = false
                        }
                    }
                    val forceRefresh = { refreshing = true }

                    // If there's an error, show the last valid state, but with an error flag
                    val uiState = setAndComputeLastGoodState(
                        uiStateFlow = uiStateFlow,
                        forceUpdate = forceRefresh
                    )

                    val ptrState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = forceRefresh
                    )

                    Box(
                        Modifier
                            .padding(innerPadding)
                            .pullRefresh(ptrState)
                    ) {
                        MainScreenContent(uiState, forceRefresh)
                        PullRefreshIndicator(
                            refreshing = refreshing,
                            state = ptrState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun setAndComputeLastGoodState(
    uiStateFlow: Flow<MainUiState>,
    forceUpdate: () -> Unit
): MainUiState {
    val uiState by uiStateFlow.collectAsStateWithLifecycle(initialValue = MainUiState.Loading)

    val (lastGoodState, setLastGoodState) = remember {
        mutableStateOf<MainUiState>(MainUiState.Loading)
    }

    setLastGoodState(
        if (uiState is MainUiState.Error && lastGoodState is MainUiState.Valid) {
            lastGoodState.copy(hasError = true)
        } else {
            uiState
        }
    )

    return lastGoodState
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun MainScreenContent(
    uiState: MainUiState,
    forceUpdate: () -> Unit
) {
    val connectivityState by LocalContext.current.observeConnectivity()
        .collectAsStateWithLifecycle(initialValue = ConnectionState.Available)

    if (uiState == MainUiState.Error) {
        ErrorScreen(
            connectivityState = connectivityState,
            forceUpdate = forceUpdate
        )
    } else {
        Crossfade(targetState = uiState == MainUiState.Loading) { isLoading ->
            when (isLoading) {
                true -> LoadingScreen()
                false -> AirportScreen(
                    uiState = uiState as MainUiState.Valid,
                    connectivityState = connectivityState
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(10.dp))
        Text(text = stringResource(R.string.loading), color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun TitleAndBackBar(
    titleAirportCode: AirportCode?,
    navController: NavHostController
) = TopAppBar(
    // Title that shows the airport code and name when an airport is selected
    // and the title of the app when no airport is selected
    title = {
        Crossfade(targetState = titleAirportCode) { title ->
            Column {
                Text(title?.shortCode ?: stringResource(id = R.string.app_name))
                Text(title?.fullName.orEmpty(), fontSize = 13.sp)
            }
        }
    },
    // Back button that shows when an airport is selected
    navigationIcon = {
        Box(modifier = Modifier.animateContentSize()) {
            if (titleAirportCode != null) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        }
    }
)

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
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp, BiasAlignment.Vertical(-0.4f))
    ) {
        AirportCode.values().toList().let {
            if (REMOVE_SWF) {
                it - AirportCode.SWF
            } else {
                it
            }
        }.forEach {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { navigateTo(it.shortCode) }
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
