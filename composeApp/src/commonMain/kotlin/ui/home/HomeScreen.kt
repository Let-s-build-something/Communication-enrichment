package ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import base.BrandBaseScreen
import base.LocalSnackbarHost
import base.currentPlatform
import base.navigation.NavIconType
import chatenrichment.composeapp.generated.resources.Res
import chatenrichment.composeapp.generated.resources.screen_home
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

/**
 * Screen for the home page
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val snackbarhostState = LocalSnackbarHost.current

    LaunchedEffect(Unit) {
        snackbarhostState?.showSnackbar(currentPlatform.toString())
    }

    BrandBaseScreen(
        title = stringResource(Res.string.screen_home),
        navIconType = NavIconType.HOME
    ) {

    }
}