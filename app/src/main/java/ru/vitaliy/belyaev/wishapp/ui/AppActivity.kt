package ru.vitaliy.belyaev.wishapp.ui

import android.content.Intent
import android.os.Bundle
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
        // Вид выбирается до первого кадра: иначе мелькнул бы не тот
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiLook.value == null || viewModel.useWallpaperColors.value == null
        }
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
}
