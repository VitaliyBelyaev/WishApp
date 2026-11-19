package ru.vitaliy.belyaev.wishapp.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppReviewRequestedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppReviewShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.old.MainRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.NavigationState
import ru.vitaliy.belyaev.wishapp.navigation.old.Navigator
import ru.vitaliy.belyaev.wishapp.navigation.old.WishDetailedRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.WishImagesViewerRoute
import ru.vitaliy.belyaev.wishapp.navigation.old.rememberNavigationState
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
internal class AppActivity : AppCompatActivity() {

    private val viewModel: AppActivityViewModel by viewModels()

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    @Inject
    lateinit var wishAppSdk: WishAppSdk

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    @ExperimentalAnimationApi
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Вид выбирается до первого кадра: иначе мелькнул бы не тот. Ниже Android 12 значок
        // заставки неподвижен, и новый вид чуть ждёт хотелок (`ReportDrawnWhen` в списке), но не
        // дольше FIRST_CONTENT_WAIT_MS. С Android 12 хотелки грузятся, пока рисуется значок, и
        // заставка их не ждёт. Не успели — список покажет заготовки карточек
        val splashShownAt = SystemClock.uptimeMillis()
        splashScreen.setKeepOnScreenCondition {
            val uiLook = viewModel.uiLook.value
            val waitsForList = uiLook == UiLook.NEW &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
                !fullyDrawnReporter.isFullyDrawnReported &&
                SystemClock.uptimeMillis() - splashShownAt < FIRST_CONTENT_WAIT_MS
            uiLook == null || viewModel.useWallpaperColors.value == null || waitsForList
        }
        splashScreen.setOnExitAnimationListener(::playSplashExit)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            viewModel.onCreateWithoutSavedInstanceState(this)
        }

        if (savedInstanceState == null) {
            intent.toIncomingShare(packageName)?.let(viewModel::onIncomingShare)
        }

        // Проверяет обновление сам при каждом onStart
        val inAppUpdateController = InAppUpdateController(
            activity = this,
            dataStoreRepository = dataStoreRepository,
            analyticsRepository = analyticsRepository,
            onUpdateDownloaded = viewModel::onUpdateDownloaded,
            onDownloadStarted = { viewModel.onUpdateDownloadStarted(getString(R.string.update_downloading_text)) },
        )
        lifecycleScope.launch {
            viewModel.completeUpdateFlow.collect { inAppUpdateController.completeUpdate() }
        }

        lifecycleScope.launch {
            viewModel.requestReviewFlow.collect {
                analyticsRepository.trackEvent(InAppReviewRequestedEvent)
                val reviewManager = ReviewManagerFactory.create(this@AppActivity)
                reviewManager
                    .requestReviewFlow()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            analyticsRepository.trackEvent(InAppReviewShowEvent)
                            reviewManager.launchReviewFlow(this@AppActivity, reviewInfo)
                        } else {
                            task.exception?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                        }
                    }
            }
        }

        setContent {
            val selectedTheme: Theme by viewModel.selectedTheme.collectAsStateWithLifecycle()
            val uiLook: UiLook? by viewModel.uiLook.collectAsStateWithLifecycle()

            when (uiLook) {
                UiLook.OLD -> OldLookContent(selectedTheme)
                UiLook.NEW -> {
                    val useWallpaperColors by viewModel.useWallpaperColors.collectAsStateWithLifecycle()
                    NewLookRoot(
                        selectedTheme = selectedTheme,
                        useWallpaperColors = useWallpaperColors ?: false,
                        analyticsRepository = analyticsRepository,
                        appViewModel = viewModel,
                    )
                }
                null -> Unit
            }
        }
    }

    /** Прежний вид. При смене вида его стек создаётся заново и начинается со списка. */
    @ExperimentalAnimationApi
    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    private fun OldLookContent(selectedTheme: Theme) {
        val navigationState: NavigationState = rememberNavigationState(
            startRoute = MainRoute,
            topLevelRoutes = setOf(MainRoute),
        )
        val navigator = remember { Navigator(navigationState) }
        val theme = if (navigationState.currentRoute is WishImagesViewerRoute) {
            Theme.DARK
        } else {
            selectedTheme
        }

        OldLookRoot(
            selectedTheme = theme,
            analyticsRepository = analyticsRepository,
            navigator = navigator,
        )

        // «Поделиться» из другого приложения: при холодном старте и в уже открытом приложении
        LaunchedEffect(key1 = Unit) {
            viewModel.incomingShare.filterNotNull().collect {
                val share = viewModel.consumeIncomingShare() ?: return@collect
                if (navigationState.currentRoute != MainRoute) {
                    navigator.goBack(MainRoute)
                    viewModel.deleteEmptyWishes().join()
                }
                navigator.navigate(WishDetailedRoute.fromShare(share))
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.toIncomingShare(packageName)?.let(viewModel::onIncomingShare)
    }

    /**
     * Выход заставки, как в прототипе: значок дорисовывается до конца, стоит SPLASH_SETTLE_MS,
     * потом уменьшается и гаснет вместе с фоном. Гасить значок отдельно нужно: на Android 12+ он
     * рисуется своим слоем, и прозрачность заставки на него не действует. Заставка
     * растворяется в экран за 200 мс. Хотелки за это время грузятся под ней; не успели — список
     * покажет заготовки карточек. Ниже Android 12 значок не анимируется, и ждать нечего. При
     * выключенных в системе анимациях задержка и выход сжимаются в ноль сами.
     */
    private fun playSplashExit(provider: SplashScreenViewProvider) {
        // Начало анимации значка система отдаёт по настенным часам, не по uptimeMillis
        val iconLeftMs = provider.iconAnimationStartMillis + provider.iconAnimationDurationMillis -
            System.currentTimeMillis()
        // Короткая пауза, когда значок уже встал, как в прототипе
        val delayMs = iconLeftMs.coerceIn(0L, SPLASH_ICON_MAX_WAIT_MS) + SPLASH_SETTLE_MS
        provider.iconView.animate()
            .setStartDelay(delayMs)
            .setDuration(SPLASH_EXIT_MS)
            .setInterpolator(splashExitEasing())
            .scaleX(SPLASH_EXIT_ICON_SCALE)
            .scaleY(SPLASH_EXIT_ICON_SCALE)
            .alpha(0f)
            .start()
        provider.view.animate()
            .setStartDelay(delayMs)
            .setDuration(SPLASH_EXIT_MS)
            .setInterpolator(LinearInterpolator())
            .alpha(0f)
            .withEndAction(provider::remove)
            .start()
    }
}

/**
 * Сколько неподвижная заставка (ниже Android 12) ждёт хотелок на холодном старте нового вида:
 * заметно дольше нужного она висеть не должна.
 */
private const val FIRST_CONTENT_WAIT_MS = 300L

private const val SPLASH_EXIT_MS = 200L
private const val SPLASH_SETTLE_MS = 50L

/** Ускорение на выходе, `cubic-bezier(.3, 0, .8, .15)` из прототипа. */
@Suppress("MagicNumber") // Точки кривой Безье, именовать их нечего
private fun splashExitEasing() = PathInterpolator(0.3f, 0f, 0.8f, 0.15f)
private const val SPLASH_EXIT_ICON_SCALE = 0.86f

/** Значок заставки рисуется около 940 мс; больше секунды не ждём, даже если система сказала иначе. */
private const val SPLASH_ICON_MAX_WAIT_MS = 1_000L
