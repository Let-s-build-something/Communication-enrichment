package base

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import components.AutoResizeText
import components.FontSizeRange
import components.navigation.AppBarHeightDp
import components.navigation.HorizontalAppBar
import components.navigation.NavigationIcon
import components.navigation.VerticalAppBar
import dev.gitlive.firebase.Firebase
import future_shared_module.theme.LocalTheme
import io.ktor.util.Platform

/**
 * Most basic all-in-one implementation of a screen with action bar, without bottom bar
 * @param navigationIcon what type of navigation icon screen should have
 * @param title capital title of the screen
 * @param subtitle lower case subtitle of the screen
 * @param actionIcons right side actions to be displayed
 * @param content screen content under the action bar
 */
@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    navigationIcon: Pair<ImageVector, String>? = null,
    title: String? = null,
    subtitle: String? = null,
    onNavigationIconClick: () -> Unit = {},
    shape: Shape = if(LocalDeviceType.current == WindowWidthSizeClass.Compact) {
        RoundedCornerShape(
            topEnd = 24.dp,
            topStart = 24.dp
        )
    }else RoundedCornerShape(
        topEnd = 24.dp,
        bottomEnd = 24.dp,
        bottomStart = 24.dp
    ),
    actionIcons: @Composable (Boolean) -> Unit = {},
    appBarVisible: Boolean = LocalHeyIamScreen.current.not(),
    containerColor: Color? = null,
    contentColor: Color = Color.Transparent,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val previousSnackbarHostState = LocalSnackbarHost.current
    val snackbarHostState = remember {
        previousSnackbarHostState ?: SnackbarHostState()
    }

    CompositionLocalProvider(
        LocalSnackbarHost provides snackbarHostState,
        LocalHeyIamScreen provides true
    ) {
        Scaffold(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            snackbarHost = {
                if(previousSnackbarHostState == null) {
                    BaseSnackbarHost(hostState = snackbarHostState)
                }
            },
            containerColor = Color.Transparent,
            contentColor = contentColor,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            content = { paddingValues ->
                Box {
                    // black bottom background in case of paddings of content
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .then(
                                if (containerColor != null) {
                                    Modifier.background(containerColor)
                                } else Modifier
                            )
                            .fillMaxWidth()
                    )

                    ShelledContent(
                        modifier = Modifier.padding(paddingValues),
                        appBarVisible = appBarVisible,
                        title = title,
                        subtitle = subtitle,
                        navigationIcon = navigationIcon,
                        actionIcons = actionIcons,
                        onNavigationIconClick = onNavigationIconClick
                    ) { platformModifier ->
                        Box(
                            modifier = contentModifier
                                .then(
                                    if (containerColor != null) {
                                        platformModifier
                                            .background(color = containerColor, shape = shape)
                                            .clip(shape)
                                    } else platformModifier
                                )
                        ) {
                            content()
                        }
                    }
                }
            }
        )
    }
}

/**
 * Content of the page shelled with appbar and navigation.
 * It is platform specific.
 */
@Composable
private fun ShelledContent(
    modifier: Modifier = Modifier,
    appBarVisible: Boolean = true,
    title: String? = null,
    subtitle: String? = null,
    navigationIcon: Pair<ImageVector, String>? = null,
    actionIcons: @Composable (Boolean) -> Unit = {},
    onNavigationIconClick: () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    Box(modifier = modifier) {
        // only mobile phones have a mobile, vertical menu bar
        if(LocalDeviceType.current == WindowWidthSizeClass.Compact) {
            MobileLayout(
                appBar = {
                    if(appBarVisible) {
                        HorizontalAppBar(
                            title = title,
                            navigationIcon = navigationIcon,
                            subtitle = subtitle,
                            actions = actionIcons,
                            onNavigationIconClick = onNavigationIconClick
                        )
                    }
                },
                content = content
            )
        } else {
            DesktopLayout(
                appBarVisible = appBarVisible,
                navigationIcon = navigationIcon,
                onNavigationIconClick = onNavigationIconClick,
                actionIcons = actionIcons,
                title = title,
                subtitle = subtitle,
                content = content
            )
        }
    }
}

@Composable
private fun MobileLayout(
    appBar: @Composable () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Column {
        appBar()
        content(Modifier.fillMaxSize())
    }
}

/**
 * Layout designed for devices medium to large sized.
 * This can be a tablet or a desktop computer.
 */
@Composable
private fun DesktopLayout(
    appBarVisible: Boolean,
    navigationIcon: Pair<ImageVector, String>?,
    onNavigationIconClick: () -> Unit,
    title: String?,
    subtitle: String?,
    actionIcons: @Composable (Boolean) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    LocalTheme.current.colors.brandMain,
                    shape = RoundedCornerShape(
                        bottomEnd = 24.dp,
                        topEnd = 36.dp,
                        bottomStart = 24.dp
                    )
                )
        ) {
            if(appBarVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navigationIcon?.let { navigationIcon ->
                        NavigationIcon(
                            onClick = onNavigationIconClick,
                            size = 38.dp,
                            tint = LocalTheme.current.colors.brandMainDark,
                            imageVector = navigationIcon.first,
                            contentDescription = navigationIcon.second
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f)
                            .heightIn(max = AppBarHeightDp.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        var fontSizeValue by remember { mutableFloatStateOf(16f) }

                        AutoResizeText(
                            modifier = Modifier.animateContentSize(),
                            text = buildAnnotatedString {
                                if(title != null) {
                                    withStyle(SpanStyle(fontSize = fontSizeValue.sp)) {
                                        append(title)
                                    }
                                }
                                if(subtitle != null) {
                                    if(title != null) append("\n")
                                    withStyle(SpanStyle(fontSize = fontSizeValue.times(0.65f).sp, fontWeight = FontWeight.Normal)) {
                                        append(subtitle)
                                    }
                                }
                            },
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = LocalTheme.current.colors.brandMainDark
                            ),
                            fontSizeRange = FontSizeRange(
                                min = 12.sp,
                                max = 16.sp
                            ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            onFontSizeChange = { fontSize ->
                                fontSizeValue = fontSize
                            }
                        )
                    }
                }
            }
            content(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }

        if(appBarVisible) {
            VerticalAppBar(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth(),
                actions = actionIcons
            )
        }
    }
}

/** Current device screen size in DP (density pixels) */
expect val LocalScreenSize: ProvidableCompositionLocal<IntSize>

/** Platform using this application */
expect val currentPlatform: Platform

/** Platform specific Firebase instance */
expect val PlatformFirebase: Firebase?

/** Indication of whether the scope below is within a screen */
val LocalHeyIamScreen = staticCompositionLocalOf { false }

/** Current device frame type */
val LocalDeviceType = staticCompositionLocalOf { WindowWidthSizeClass.Medium }

/** current snackbar host for showing snackbars */
val LocalSnackbarHost = staticCompositionLocalOf<SnackbarHostState?> { null }

/** Default page size based on current device tye */
val LocalNavController = staticCompositionLocalOf<NavHostController?> { null }

/** Custom on back pressed provided by parent */
val LocalOnBackPressDispatcher = staticCompositionLocalOf<(() -> Unit)?> { null }