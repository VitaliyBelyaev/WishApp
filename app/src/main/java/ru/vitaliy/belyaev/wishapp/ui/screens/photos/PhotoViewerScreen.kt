package ru.vitaliy.belyaev.wishapp.ui.screens.photos

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.ui.core.findActivity
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras

/** Куда просмотр отдаёт закрытие и сообщения: снэкбар у приложения общий, над навигацией. */
class PhotoViewerActions(
    val onClose: () -> Unit,
    val onShowMessage: (AppSnackMessage) -> Unit,
)

/** Действия кнопок просмотра. */
internal class ViewerButtonsActions(
    val onBack: () -> Unit,
    val onMakeMain: (ImageRef) -> Unit,
    val onAdd: () -> Unit,
    val onDelete: (ImageRef) -> Unit,
)

/** Метки для UI-тестов просмотра. */
object PhotoViewerTestTags {
    const val SCREEN = "photo_viewer"
    const val PAGER = "photo_viewer_pager"
    const val BACK = "photo_viewer_back"
    const val COUNTER = "photo_viewer_counter"
    const val MAKE_MAIN = "photo_viewer_make_main"
    const val ADD = "photo_viewer_add"
    const val DELETE = "photo_viewer_delete"
}

/**
 * Просмотр фото на весь экран, как в галерее (`ScreenViewer`): чёрный фон в любой теме, фото под
 * системными панелями. Нажатие прячет кнопки и статус-бар, свайп вверх или вниз закрывает, пока фото
 * не увеличено.
 *
 * Лежит слоем поверх хотелки (`OverlaySceneStrategy`): при свайпе фон гаснет и под ним видна хотелка.
 * Фото вырастает из своего места на хотелке и сжимается обратно, как в Telegram ([PhotoHeroLink]).
 * Если места не нашлось, просмотр проявляется и гаснет. Вход и выход просмотр анимирует сам
 * и только потом просит навигацию убрать его.
 */
@Composable
fun PhotoViewerScreen(
    wishId: String,
    initialIndex: Int,
    actions: PhotoViewerActions,
    modifier: Modifier = Modifier,
    heroLink: PhotoHeroLink? = null,
    viewModel: PhotoViewerViewModel =
        hiltViewModel<PhotoViewerViewModel, PhotoViewerViewModel.Factory>(key = "viewer_$wishId") {
            it.create(wishId)
        },
) {
    val images by viewModel.images.collectAsStateWithLifecycle()
    val currentActions by rememberUpdatedState(actions)
    val resources = LocalResources.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Ждём, когда добавленные фото появятся, чтобы показать последнее
    var awaitingAdded by rememberSaveable { mutableStateOf(false) }
    var current by remember { mutableStateOf<ImageRef?>(null) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val transition = rememberViewerTransition(heroLink, wishId, current)
    LaunchedEffect(transition) { transition.enter(awaitImage = { snapshotFlow { current }.first { it != null } }) }
    val close: (fromOffset: Float) -> Unit = { transition.close(scope, it, currentActions.onClose) }
    // «Назад» без предиктивной анимации, как в галереях и Telegram: жест ничего не двигает,
    // фото сжимается, когда его отпустили
    BackHandler(enabled = !transition.closing) { close(0f) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                PhotoViewerEvent.MainSet -> AppSnackMessage(resources.getString(R.string.snack_main_photo))
                PhotoViewerEvent.AddFailed -> {
                    awaitingAdded = false
                    AppSnackMessage(resources.getString(R.string.photo_add_failed))
                }
                is PhotoViewerEvent.Deleted -> AppSnackMessage(
                    text = resources.getString(R.string.snack_photo_deleted),
                    actionLabel = resources.getString(R.string.snack_undo),
                    onAction = event.onUndo,
                    onDismiss = event.onConfirm,
                )
            }
            currentActions.onShowMessage(message)
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
        if (it.isNotEmpty()) awaitingAdded = true
        viewModel.onImagesPicked(it, context.contentResolver)
    }

    // После удаления последнего фото просмотр закрывается
    LaunchedEffect(viewModel) {
        viewModel.images.first { it?.isEmpty() == true }
        close(0f)
    }

    val loaded = images
    var uiVisible by rememberSaveable { mutableStateOf(true) }
    ViewerSystemBars(barsVisible = uiVisible, closing = transition.closing, photosBehind = !loaded.isNullOrEmpty())

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { transition.origin = it.positionInRoot() }
            .graphicsLayer { alpha = transition.contentAlpha }
            .testTag(PhotoViewerTestTags.SCREEN),
    ) {
        // Фон гаснет и при переходе, и когда фото тащат: под ним видна хотелка
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val dragFade = (1f - dragFraction * BACKGROUND_FADE_SPEED).coerceIn(0f, 1f)
                    alpha = transition.backgroundAlpha * dragFade
                }
                .background(Color.Black),
        )
        if (!loaded.isNullOrEmpty()) {
            ViewerContent(
                images = loaded,
                initialIndex = initialIndex,
                state = ViewerContentState(
                    uiVisible = uiVisible,
                    buttonsAllowed = transition.opened && !transition.layerShown,
                    pagerHidden = transition.layerShown,
                    flyAway = !transition.canCollapse,
                ),
                awaitingAdded = awaitingAdded,
                onAddedShown = { awaitingAdded = false },
                onToggleUi = { uiVisible = !uiVisible },
                pagerCallbacks = ViewerPagerCallbacks(
                    onCurrent = { image ->
                        current = image
                        heroLink?.viewedImageId = image.id
                    },
                    onDragFraction = { dragFraction = it },
                    onDismiss = { offset ->
                        if (transition.canCollapse) close(offset) else transition.closeNow(currentActions.onClose)
                    },
                ),
                actions = ViewerButtonsActions(
                    onBack = { close(0f) },
                    onMakeMain = viewModel::onMakeMain,
                    onAdd = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onDelete = viewModel::onDelete,
                ),
            )
        }
        ViewerHeroLayer(transition)
    }
}

/** Что сейчас можно показать: кнопки, пейджер; [flyAway] — фото некуда сжиматься, при свайпе оно улетает. */
internal class ViewerContentState(
    val uiVisible: Boolean,
    val buttonsAllowed: Boolean,
    val pagerHidden: Boolean,
    val flyAway: Boolean,
)

/** Что пейджер сообщает экрану: открытое фото, насколько его утащили, отпустили для закрытия. */
internal class ViewerPagerCallbacks(
    val onCurrent: (ImageRef) -> Unit,
    val onDragFraction: (Float) -> Unit,
    val onDismiss: (offset: Float) -> Unit,
)

@Composable
private fun ViewerContent(
    images: List<ImageRef>,
    initialIndex: Int,
    state: ViewerContentState,
    awaitingAdded: Boolean,
    onAddedShown: () -> Unit,
    onToggleUi: () -> Unit,
    pagerCallbacks: ViewerPagerCallbacks,
    actions: ViewerButtonsActions,
) {
    // Пейджер запоминает лямбды при первом показе, а список фото меняется: читаем всегда свежий
    val currentImages by rememberUpdatedState(images)
    val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(0, images.lastIndex)) { currentImages.size }
    // Насколько фото утащили: 0 на месте, 1 на всю высоту. Кнопки прячутся
    var dragFraction by remember { mutableFloatStateOf(0f) }
    ScrollToAdded(pagerState, images.size, awaitingAdded, onAddedShown)
    val callbacks by rememberUpdatedState(pagerCallbacks)
    LaunchedEffect(pagerState) {
        snapshotFlow { currentImages.getOrNull(pagerState.currentPage) }
            .filterNotNull()
            .collect { callbacks.onCurrent(it) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Фото листаются по ключу: «Сделать главным» переносит фото в начало, а на экране остаётся оно же
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (state.pagerHidden) 0f else 1f }
                .testTag(PhotoViewerTestTags.PAGER),
            key = { currentImages[it].id },
        ) { page ->
            val image = currentImages.getOrNull(page) ?: return@HorizontalPager
            ViewerPage(
                image = image,
                isActive = page == pagerState.settledPage,
                onTap = onToggleUi,
                onDragFraction = {
                    dragFraction = it
                    callbacks.onDragFraction(it)
                },
                flyAway = state.flyAway,
                onDismiss = { callbacks.onDismiss(it) },
            )
        }
        val current = images.getOrNull(pagerState.currentPage) ?: images.first()
        AnimatedVisibility(
            visible = state.uiVisible && state.buttonsAllowed && dragFraction == 0f,
            // Фото сжимается в хотелку: кнопки пропадают сразу, а не гаснут поверх перехода
            modifier = Modifier.graphicsLayer { alpha = if (state.pagerHidden) 0f else 1f },
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ViewerButtons(
                position = pagerState.currentPage,
                count = images.size,
                isMain = current.id == images.first().id,
                wide = maxWidth > maxHeight,
                onBack = actions.onBack,
                onMakeMain = { actions.onMakeMain(current) },
                onAdd = actions.onAdd,
                onDelete = { actions.onDelete(current) },
            )
        }
    }
}

/** Добавленные фото встают в конец: когда они появились, листаем к последнему. */
@Composable
private fun ScrollToAdded(pagerState: PagerState, count: Int, awaitingAdded: Boolean, onAddedShown: () -> Unit) {
    var previousCount by remember { mutableIntStateOf(count) }
    val currentOnAddedShown by rememberUpdatedState(onAddedShown)
    LaunchedEffect(count) {
        if (awaitingAdded && count > previousCount) {
            pagerState.animateScrollToPage(count - 1)
            currentOnAddedShown()
        }
        previousCount = count
    }
}

/**
 * Системные панели в просмотре: значки светлые на чёрном, нажатие прячет статус-бар вместе с кнопками,
 * навигация остаётся.
 * Как только просмотр начал закрываться, панели возвращаются, значки — как у экрана под ним:
 * над фото хотелки светлые.
 */
@Composable
private fun ViewerSystemBars(barsVisible: Boolean, closing: Boolean, photosBehind: Boolean) {
    val view = LocalView.current
    val controller = remember(view) {
        view.context.findActivity()?.window?.let { WindowCompat.getInsetsController(it, view) }
    }
    val themeLight = !AppThemeExtras.isDark
    val visible = rememberUpdatedState(barsVisible)
    val leaving = rememberUpdatedState(closing)
    val behind = rememberUpdatedState(photosBehind)
    val restore: WindowInsetsControllerCompat.() -> Unit = {
        show(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        isAppearanceLightNavigationBars = themeLight
        isAppearanceLightStatusBars = if (behind.value) false else themeLight
    }
    LaunchedEffect(controller, themeLight) {
        val bars = controller ?: return@LaunchedEffect
        snapshotFlow { leaving.value to visible.value }
            .collect { (isLeaving, shown) ->
                if (isLeaving) {
                    bars.restore()
                } else {
                    bars.isAppearanceLightStatusBars = false
                    bars.isAppearanceLightNavigationBars = false
                    bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    // Прячем только статус-бар, навигация остаётся, как в Telegram: жесты «домой»
                    // и «назад» с краю работают с первого раза, а не сначала возвращают панель
                    if (shown) {
                        bars.show(WindowInsetsCompat.Type.statusBars())
                    } else {
                        bars.hide(WindowInsetsCompat.Type.statusBars())
                    }
                }
            }
    }
    DisposableEffect(controller, themeLight) {
        onDispose { controller?.restore() }
    }
}

/** Насколько быстрее пальца гаснет фон: при трети высоты он уже прозрачный. */
private const val BACKGROUND_FADE_SPEED = 3f
