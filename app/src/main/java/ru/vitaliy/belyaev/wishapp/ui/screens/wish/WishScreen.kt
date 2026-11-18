package ru.vitaliy.belyaev.wishapp.ui.screens.wish

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.BulkAction
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent

/**
 * С чем экран хотелки вернулся на список: [action] `null` у простого «назад». Список дожидается
 * записей в базу, пустую хотелку удаляет, а действие показывает снэкбаром с «Вернуть» и пишет в базу,
 * когда снэкбар закрылся.
 */
data class WishScreenResult(val wishId: String, val action: BulkAction? = null)

/** Действия экрана хотелки, которые ведут на другие экраны. */
class WishScreenActions(
    val onFinished: (WishScreenResult) -> Unit,
    val onOpenPhoto: (wishId: String, index: Int) -> Unit,
    val onShowMessage: (AppSnackMessage) -> Unit,
)

/** Поля ввода: название и комментарий живут во ViewModel, из базы берутся только при открытии. */
data class WishFields(
    val title: String,
    val comment: String,
    val linkInput: String,
    val isLinkInvalid: Boolean,
)

/**
 * Экран хотелки нового вида (`ScreenDetail`). Режима редактирования нет: название и комментарий
 * сразу поля, всё сохраняется само. Фото на первом плане, при прокрутке текст наезжает на него
 * и останавливается ниже кнопок. Удаление, «Сбылось» и «Снова хочу» возвращают на список,
 * отменить их можно в снэкбаре. Пустая хотелка удаляется при выходе, тоже с «Вернуть».
 * Правки пишет [WishWriteQueue], поэтому экран можно закрыть в любой момент.
 */
@Composable
fun WishScreen(
    wishId: String,
    actions: WishScreenActions,
    modifier: Modifier = Modifier,
    viewModel: WishViewModel = hiltViewModel<WishViewModel, WishViewModel.Factory>(key = wishId) { it.create(wishId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val comment by viewModel.comment.collectAsStateWithLifecycle()
    val linkInput by viewModel.linkInput.collectAsStateWithLifecycle()
    val isLinkInvalid by viewModel.isLinkInvalid.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val currentActions by rememberUpdatedState(actions)
    // Уход не ждёт записей в базу: их дожидается список. Поэтому системный «назад» не перехвачен
    // и работает предиктивно, а на список возвращается так же, через AppNavigation
    val leave: (BulkAction?) -> Unit = { action -> currentActions.onFinished(WishScreenResult(wishId, action)) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WishScreenEvent.PhotoFailed ->
                    currentActions.onShowMessage(AppSnackMessage(resources.getString(R.string.photo_add_failed)))
            }
        }
    }

    val loaded = state
    if (loaded == null) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
    } else {
        val editor: WishEditor = viewModel
        val tagsEditor: WishTagsEditor = viewModel
        WishContent(
            state = loaded,
            fields = WishFields(title.orEmpty(), comment.orEmpty(), linkInput, isLinkInvalid),
            editor = editor,
            tagsEditor = tagsEditor,
            onLeave = leave,
            onOpenPhoto = { currentActions.onOpenPhoto(wishId, it) },
            onShowMessage = { currentActions.onShowMessage(it) },
            modifier = modifier,
        )
    }
}

/** Что открыто поверх экрана: шторка ярлыка, ссылки или выбор ярлыков. */
internal sealed interface WishOverlay {
    data class Tag(val tag: TagEntity) : WishOverlay
    data class Link(val link: String) : WishOverlay
    data object TagPicker : WishOverlay
}

@Composable
private fun WishContent(
    state: WishScreenState,
    fields: WishFields,
    editor: WishEditor,
    tagsEditor: WishTagsEditor,
    onLeave: (BulkAction?) -> Unit,
    onOpenPhoto: (Int) -> Unit,
    onShowMessage: (AppSnackMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val wish = state.wish
    val context = LocalContext.current
    val resources = LocalResources.current
    var overlay by remember { mutableStateOf<WishOverlay?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
        editor.onImagesPicked(it, context.contentResolver)
    }
    val pickPhotos = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val openLink: (String) -> Unit = { link ->
        if (!context.openLink(link)) onShowMessage(AppSnackMessage(resources.getString(R.string.link_open_failed)))
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag(WishScreenTestTags.SCREEN),
    ) {
        val hasPhotos = wish.images.isNotEmpty()
        val geometry = rememberHeroGeometry(hasPhotos = hasPhotos, maxWidth = maxWidth, maxHeight = maxHeight)
        val scrollState = rememberScrollState()
        val density = LocalDensity.current
        val rangePx = with(density) { (geometry.max - geometry.min).toPx() }
        val collapse = rememberSaveable(hasPhotos, saver = HeroCollapseState.Saver) { HeroCollapseState() }
        val connection = remember(collapse, rangePx, scrollState) { HeroCollapse(collapse, rangePx, scrollState) }
        CollapseOnKeyboard(collapse, rangePx)
        val heroPx = { with(density) { geometry.max.toPx() } - collapse.collapsedPx }
        val fade = { if (rangePx <= 0f) 1f else (1f - collapse.collapsedPx / (rangePx * FADE_PART)).coerceIn(0f, 1f) }

        WishHero(wish = wish, heroPx = heroPx, fade = fade, onOpenPhoto = onOpenPhoto, onAddPhoto = pickPhotos)
        if (hasPhotos) PhotoStatusBar()
        WishSheetBody(
            wish = wish,
            fields = fields,
            editor = editor,
            scrollState = scrollState,
            connection = connection,
            topPx = { heroPx() - with(density) { SheetOverlap.toPx() } },
            onOpenLink = openLink,
            onOverlay = { overlay = it },
        )
        TopButtons(
            hasPhotos = hasPhotos,
            onBack = { onLeave(null) },
            onAddPhoto = pickPhotos,
            onShare = { context.startActivity(createSharePlainTextIntent(wish.shareText())) },
            onDelete = { onLeave(BulkAction.DELETE) },
        )
        BottomActions(
            wish = wish,
            onOpenLink = { wish.links.firstOrNull()?.let(openLink) },
            onLeave = onLeave,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    WishOverlays(
        overlay = overlay,
        state = state,
        editor = editor,
        tagsEditor = tagsEditor,
        onDismiss = { overlay = null },
        onUndoable = { text, arg, undo ->
            onShowMessage(
                AppSnackMessage(
                    text = resources.getString(text, arg),
                    actionLabel = resources.getString(R.string.snack_undo),
                    onAction = undo,
                ),
            )
        },
        onCopied = { link ->
            context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText(null, link))
            onShowMessage(AppSnackMessage(resources.getString(R.string.snack_link_copied)))
        },
    )
}

/** Высота фото: сколько при открытии и до скольких сворачивается, чтобы контент встал ниже кнопок. */
private class HeroGeometry(val max: Dp, val min: Dp)

@Composable
private fun rememberHeroGeometry(hasPhotos: Boolean, maxWidth: Dp, maxHeight: Dp): HeroGeometry {
    val top = with(LocalDensity.current) { WindowInsets.safeDrawing.getTop(this).toDp() }
    // Контент останавливается на 12 dp ниже кнопок, у хотелки без фото он сразу там
    val min = top + ButtonsTop + FloatingButtonSize + WishAppSpacing.s3 + SheetOverlap
    val max = if (hasPhotos) {
        (maxWidth * HERO_RATIO).coerceAtMost(maxHeight * HERO_MAX_SHARE).coerceAtLeast(min)
    } else {
        min
    }
    return HeroGeometry(max = max, min = min)
}

/** Насколько свёрнуто фото, в пикселях. Переживает поворот. */
@Stable
internal class HeroCollapseState(initial: Float = 0f) {
    var collapsedPx by mutableFloatStateOf(initial)

    companion object {
        val Saver = Saver<HeroCollapseState, Float>(save = { it.collapsedPx }, restore = { HeroCollapseState(it) })
    }
}

/**
 * Прокрутка вверх сначала сворачивает фото, потом листает текст. Вниз наоборот: фото раскрывается,
 * когда текст уже в начале. Текст коротких хотелок, который весь помещается, не двигается.
 */
private class HeroCollapse(
    private val state: HeroCollapseState,
    private val rangePx: Float,
    private val scrollState: ScrollState,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y >= 0f || scrollState.maxValue == 0) return Offset.Zero
        val old = state.collapsedPx
        val new = (old - available.y).coerceAtMost(rangePx)
        state.collapsedPx = new
        return Offset(0f, old - new)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (available.y <= 0f) return Offset.Zero
        val old = state.collapsedPx
        val new = (old - available.y).coerceAtLeast(0f)
        state.collapsedPx = new
        return Offset(0f, old - new)
    }
}

/** С клавиатурой фото уходит, чтобы поле было видно. */
@Composable
private fun CollapseOnKeyboard(state: HeroCollapseState, rangePx: Float) {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isImeVisible, rangePx) {
        if (isImeVisible && state.collapsedPx < rangePx) {
            animate(state.collapsedPx, rangePx) { value, _ -> state.collapsedPx = value }
        }
    }
}

@Composable
private fun WishHero(
    wish: WishEntity,
    heroPx: () -> Float,
    fade: () -> Float,
    onOpenPhoto: (Int) -> Unit,
    onAddPhoto: () -> Unit,
) {
    val tone = wish.shownTag()?.color?.toPaperTone() ?: PaperTone.PEACH
    val heroModifier = Modifier
        .fillMaxWidth()
        .layout { measurable, constraints ->
            val height = heroPx().toInt().coerceAtLeast(0)
            val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
            layout(placeable.width, height) { placeable.place(0, 0) }
        }
        .background(AppThemeExtras.colors.paper(tone))
    if (wish.images.isEmpty()) {
        Box(modifier = heroModifier)
    } else {
        val pagerState = rememberPagerState { wish.images.size }
        WishPhotos(
            images = wish.images,
            pagerState = pagerState,
            onPhotoClick = onOpenPhoto,
            onAddPhoto = onAddPhoto,
            fade = fade,
            modifier = heroModifier,
        )
    }
}

/** Верх кнопок под статус-баром, как у шапки главного: строка 56 dp, кнопка 48 dp по центру. */
internal val ButtonsTop = 4.dp

/** Фото 340 dp при ширине 360 dp, но не выше 55% экрана. */
private const val HERO_RATIO = 0.94f
private const val HERO_MAX_SHARE = 0.55f

/** Точки и «Фото» гаснут за первые 60% сворачивания. */
private const val FADE_PART = 0.6f

/**
 * Статус-бар над фото в любой теме как в тёмной: светлые значки на тёмной подложке. Светлая подложка,
 * как у шапки главного, на фото слишком заметна. При открытии значки меняются на середине перехода,
 * при уходе, в том числе жестом «назад», — когда от экрана хотелки осталось 30 %.
 */
@Composable
private fun PhotoStatusBar() {
    val view = LocalView.current
    val controller = remember(view) {
        view.context.findActivity()?.window?.let { WindowCompat.getInsetsController(it, view) }
    }
    // Значки темы, как их ставит `AppTheme`
    val themeLight = !AppThemeExtras.isDark
    // Насколько экран на месте: 1 — открыт, 0 — уехал. Кривая та же, что у сдвига экрана
    // в `AppNavigation`, поэтому значение — доля сдвига. При жесте «назад» идёт за пальцем
    val transition = LocalNavAnimatedContentScope.current.transition
    val shown = transition.animateFloat(
        transitionSpec = { tween(PHOTO_ICONS_MILLIS, easing = FastOutSlowInEasing) },
        label = "photo_status_bar",
    ) { if (it == EnterExitState.Visible) 1f else 0f }
    LaunchedEffect(controller, themeLight) {
        snapshotFlow {
            val leaving = transition.targetState != EnterExitState.Visible
            shown.value > if (leaving) PHOTO_ICONS_LEAVE_AT else PHOTO_ICONS_ENTER_AT
        }
            .collect { isShown -> controller?.isAppearanceLightStatusBars = if (isShown) false else themeLight }
    }
    DisposableEffect(controller, themeLight) {
        onDispose { controller?.isAppearanceLightStatusBars = themeLight }
    }
    val statusBar = with(LocalDensity.current) { WindowInsets.safeDrawing.getTop(this).toDp() }
    val total = statusBar + PhotoScrimTail
    val brush = remember(statusBar) {
        val plateau = (statusBar / total * PHOTO_SCRIM_PLATEAU).coerceIn(0f, 1f)
        // Плотно у верхнего края, дальше гаснет по smoothstep без заметной границы. Цвета через равные доли
        val colors = List(PHOTO_SCRIM_STEPS + 1) { i ->
            val y = i / PHOTO_SCRIM_STEPS.toFloat()
            val u = ((y - plateau) / (1f - plateau)).coerceIn(0f, 1f)
            Color.Black.copy(alpha = PHOTO_SCRIM_ALPHA * (1f - u * u * (3f - 2f * u)))
        }
        Brush.verticalGradient(colors)
    }
    Box(modifier = Modifier.fillMaxWidth().height(total).background(brush))
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Насколько подложка заходит ниже статус-бара. */
private val PhotoScrimTail = 24.dp
private const val PHOTO_SCRIM_ALPHA = 0.45f
private const val PHOTO_SCRIM_PLATEAU = 0.3f
private const val PHOTO_SCRIM_STEPS = 16

/** Как переход между экранами в `AppNavigation`. */
private const val PHOTO_ICONS_MILLIS = 300

// Значки при открытии — на середине, при уходе — когда от экрана осталось 30 %
private const val PHOTO_ICONS_ENTER_AT = 0.5f
private const val PHOTO_ICONS_LEAVE_AT = 0.3f
