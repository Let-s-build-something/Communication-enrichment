package components.pull_refresh

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import base.BrandBaseScreen
import base.navigation.NavIconType
import augmy.interactive.shared.ui.base.LocalDeviceType
import augmy.interactive.shared.ui.components.getDefaultPullRefreshSize
import augmy.interactive.shared.ui.components.navigation.AppBarHeightDp

/**
 * Implementation of the [BrandBaseScreen] with pull to refresh logic
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefreshableScreen(
    modifier: Modifier = Modifier,
    viewModel: RefreshableViewModel,
    navIconType: NavIconType? = null,
    title: String? = null,
    subtitle: String? = null,
    onBackPressed: () -> Boolean = { true },
    actionIcons: (@Composable (expanded: Boolean) -> Unit)? = null,
    onNavigationIconClick: (() -> Unit)? = null,
    appBarVisible: Boolean = true,
    contentColor: Color = Color.Transparent,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val refreshScope = rememberCoroutineScope()
    val pullRefreshSize = getDefaultPullRefreshSize(
        isSmallDevice = LocalDeviceType.current == WindowWidthSizeClass.Compact
    )
    val indicatorOffset = remember { mutableStateOf(0.dp) }
    val indicatorWidth = remember { mutableStateOf(0f) }

    val isRefreshing = viewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            viewModel.requestData(scope = refreshScope, isSpecial = true, isPullRefresh = true)
        },
        refreshingOffset = pullRefreshSize.plus(AppBarHeightDp.dp),
        refreshThreshold = pullRefreshSize
    )

    DraggableRefreshIndicator(
        modifier = Modifier
            .width(with(LocalDensity.current) {
                indicatorWidth.value.toDp()
            })
            .systemBarsPadding()
            .statusBarsPadding()
            .zIndex(100f),
        pullRefreshSize = pullRefreshSize,
        state = pullRefreshState,
        isRefreshing = isRefreshing.value
    ) { indicatorOffsetDp ->
        indicatorOffset.value = indicatorOffsetDp
    }

    BrandBaseScreen(
        modifier = modifier,
        navIconType = navIconType,
        title = title,
        subtitle = subtitle,
        onBackPressed = onBackPressed,
        actionIcons = actionIcons,
        contentModifier = Modifier
            .graphicsLayer {
                translationY = indicatorOffset.value
                    .roundToPx()
                    .toFloat()
                indicatorWidth.value = size.width
            }
            .pullRefresh(pullRefreshState),
        content = content,
        onNavigationIconClick = onNavigationIconClick,
        appBarVisible = appBarVisible,
        contentColor = contentColor,
        floatingActionButtonPosition = floatingActionButtonPosition,
        floatingActionButton = floatingActionButton
    )
}