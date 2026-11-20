package ru.vitaliy.belyaev.wishapp.ui.screens.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.createAppFeedback
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeader
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeaderCollapse
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SectionLabel
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsList
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.SettingsRow
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppRadius
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.displayM
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow
import ru.vitaliy.belyaev.wishapp.utils.createSendEmailIntent
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow
import timber.log.Timber

/** Куда ведёт «О приложении»: лицензии и политика открываются внутри приложения. */
class AboutActions(
    val onBack: () -> Unit,
    val onLicenses: () -> Unit,
    val onPolicy: () -> Unit,
    val onShowMessage: (AppSnackMessage) -> Unit,
)

/**
 * «О приложении» нового вида (экран `about` в прототипе): значок, название, версия, карточка открытого кода,
 * письмо разработчикам, лицензии и политика.
 */
@Composable
fun AboutScreen(actions: AboutActions, viewModel: AboutViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val resources = LocalResources.current

    trackScreenShow { viewModel.trackScreenShow() }

    AboutContent(
        version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        callbacks = object : AboutCallbacks {
            override fun onBack() = actions.onBack()
            override fun onSourceCode() {
                viewModel.onSourceCodeClicked()
                context.openInBrowser(SOURCE_CODE_URL)
            }
            override fun onWrite() {
                val feedback = createAppFeedback(resources)
                val intent = createSendEmailIntent(feedback.email, feedback.subject, feedback.message)
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    actions.onShowMessage(AppSnackMessage(text = resources.getString(R.string.no_email_app_error)))
                }
            }
            override fun onLicenses() {
                viewModel.onLicensesClicked()
                actions.onLicenses()
            }
            override fun onPolicy() = actions.onPolicy()
        },
    )
}

internal interface AboutCallbacks {
    fun onBack()
    fun onSourceCode()
    fun onWrite()
    fun onLicenses()
    fun onPolicy()
}

@Composable
internal fun AboutContent(version: String, callbacks: AboutCallbacks) {
    val scrollState = rememberScrollState()
    var eggPlay by remember { mutableIntStateOf(0) }
    var iconBounds by remember { mutableStateOf(Rect.Zero) }
    var isEggPlaying by remember { mutableStateOf(false) }
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    val progress: () -> Float = { (scrollState.value / collapsePx).coerceIn(0f, 1f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(start = WishAppSpacing.s4, end = WishAppSpacing.s4, top = headerHeight() + WishAppSpacing.s2)
                .testTag(AboutTestTags.CONTENT),
        ) {
            AppIdentity(
                version = version,
                isIconHidden = isEggPlaying,
                onIconBounds = { iconBounds = it },
                onEgg = { eggPlay++ },
            )
            OpenSourceCard(onClick = callbacks::onSourceCode)
            SectionLabel(stringResource(R.string.settings_contact))
            SettingsList {
                SettingsRow(
                    icon = R.drawable.ic_ms_mail,
                    title = stringResource(R.string.feedback),
                    subtitle = stringResource(R.string.settings_contact_sub),
                    isExternal = true,
                    onClick = callbacks::onWrite,
                )
            }
            SectionLabel(stringResource(R.string.settings_docs))
            SettingsList {
                SettingsRow(
                    icon = R.drawable.ic_ms_description,
                    title = stringResource(R.string.licenses),
                    onClick = callbacks::onLicenses,
                )
                SettingsRow(
                    icon = R.drawable.ic_ms_policy,
                    title = stringResource(R.string.privacy_policy),
                    onClick = callbacks::onPolicy,
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
        PageHeader(title = stringResource(R.string.about_app), progress = progress, onBack = callbacks::onBack)
        AboutEasterEgg(play = eggPlay, iconBounds = iconBounds, onPlaying = { isEggPlaying = it }) { AppIconImage() }
    }
}

/**
 * Значок, название и версия по центру. Быстрые нажатия на значок или название запускают пасхалку ([onEgg]):
 * каждое нажатие слегка сплющивает значок, без ряби и без озвучки, пасхалка тайная.
 */
@Composable
private fun AppIdentity(version: String, isIconHidden: Boolean, onIconBounds: (Rect) -> Unit, onEgg: () -> Unit) {
    val streak = remember { TapStreak() }
    val squish = remember { Animatable(1f) }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val currentIsHidden by rememberUpdatedState(isIconHidden)
    val tapModifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            if (currentIsHidden) return@detectTapGestures
            val taps = streak.tap(SystemClock.uptimeMillis())
            if (taps >= TAPS_TO_PLAY) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onEgg()
            } else {
                if (taps >= TAPS_TO_HINT) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                scope.launch {
                    squish.snapTo(1f - TAP_SQUISH * taps)
                    squish.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = WishAppSpacing.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = tapModifier
                .onGloballyPositioned { onIconBounds(it.boundsInRoot()) }
                .graphicsLayer {
                    scaleX = squish.value
                    scaleY = squish.value
                    alpha = if (isIconHidden) 0f else 1f
                }
                .testTag(AboutTestTags.ICON),
        ) { AppIconImage() }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayM.copy(fontSize = 24.sp, lineHeight = 30.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = WishAppSpacing.s4, bottom = 2.dp)
                .then(tapModifier),
        )
        Text(
            text = stringResource(R.string.app_version_pattern, version),
            style = AboutBodyStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Значок приложения из слоёв адаптивного значка: фон и знак. Слои 108 dp, на экране видна середина 72 dp,
 * поэтому рисуем их крупнее и обрезаем. Новый фон значка (этап 2.11) появится здесь сам.
 */
@Composable
private fun AppIconImage() {
    val shape = RoundedCornerShape(AppIconRadius)
    Box(
        modifier = Modifier
            .size(AppIconSize)
            .tokenShadow(shape, WishAppElevation.dock(AppThemeExtras.isDark))
            .clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        val layer = Modifier.requiredSize(AppIconSize * ADAPTIVE_ICON_SCALE)
        Image(
            painter = painterResource(R.mipmap.ic_launcher_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = layer,
        )
        Image(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = layer)
    }
}

/** Карточка «Открытый код» на `paper-mint`: вся карточка ведёт на GitHub. */
@Composable
private fun OpenSourceCard(onClick: () -> Unit) {
    val onCard = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WishAppRadius.lg))
            .background(AppThemeExtras.colors.paper(PaperTone.MINT))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(OpenSourceCardPadding)
            .testTag(AboutTestTags.SOURCE_CODE),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(painter = painterResource(R.drawable.ic_ms_code), contentDescription = null, tint = onCard)
        Column(verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s1)) {
            Text(
                text = stringResource(R.string.settings_open_source),
                style = MaterialTheme.typography.titleM,
                color = onCard,
            )
            Text(text = stringResource(R.string.about_open_source_text), style = AboutBodyStyle, color = onCard)
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = SOURCE_CODE_LABEL,
                    style = MaterialTheme.typography.label.copy(fontSize = 13.sp),
                    color = onCard,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_ms_arrow_outward),
                    contentDescription = null,
                    tint = onCard,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Открыть ссылку в браузере; без браузера ничего не делаем. */
internal fun Context.openInBrowser(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (error: ActivityNotFoundException) {
        Timber.w(error, "No browser for $url")
    }
}

object AboutTestTags {
    const val CONTENT = "about_content"
    const val SOURCE_CODE = "about_source_code"
    const val ICON = "about_icon"
    const val EASTER_EGG = "about_easter_egg"
}

private const val SOURCE_CODE_URL = "https://github.com/VitaliyBelyaev/WishApp"
private const val SOURCE_CODE_LABEL = "github.com/VitaliyBelyaev/WishApp"

/** С какого нажатия подряд отвечаем вибрацией: намёк, что что-то будет. */
private const val TAPS_TO_HINT = 4
private const val TAP_SQUISH = 0.015f
private const val ADAPTIVE_ICON_SCALE = 108f / 72f
private val AppIconSize = 80.dp
private val AppIconRadius = 22.dp
private val OpenSourceCardPadding = 18.dp

private val AboutBodyStyle: TextStyle
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp)
