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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppReviewRequestedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppReviewShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.MainRoute
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.navigation.NavigationState
import ru.vitaliy.belyaev.wishapp.navigation.Navigator
import ru.vitaliy.belyaev.wishapp.navigation.WishDetailedRoute
import ru.vitaliy.belyaev.wishapp.navigation.WishImagesViewerRoute
import ru.vitaliy.belyaev.wishapp.navigation.rememberNavigationState
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedTextParser
import ru.vitaliy.belyaev.wishapp.shared.domain.SharedWishDraft
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppTheme

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

    private var sharedDraftFromAnotherApp: SharedWishDraft? = null

    private val sharedDraftFlow = MutableSharedFlow<SharedWishDraft>()

    @ExperimentalAnimationApi
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            viewModel.onCreateWithoutSavedInstanceState(this)
        }

        if (savedInstanceState == null) {
            sharedDraftFromAnotherApp = extractSharedDraft(intent)
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

            val navigationState: NavigationState = rememberNavigationState(
                startRoute = MainRoute,
                topLevelRoutes = setOf(MainRoute)
            )
            val navigator = remember { Navigator(navigationState) }
            val theme = if (navigationState.currentRoute is WishImagesViewerRoute) {
                Theme.DARK
            } else {
                selectedTheme
            }

            WishAppTheme(selectedTheme = theme) {
                Navigation(
                    analyticsRepository = analyticsRepository,
                    navigator = navigator,
                )
            }

            LaunchedEffect(key1 = Unit) {
                sharedDraftFromAnotherApp?.let { draft ->
                    navigator.navigate(WishDetailedRoute.fromSharedDraft(draft))
                    sharedDraftFromAnotherApp = null
                }
            }

            LaunchedEffect(key1 = Unit) {
                sharedDraftFlow.collect { draft ->
                    if (navigationState.currentRoute != MainRoute) {
                        navigator.goBack(MainRoute)
                        viewModel.deleteEmptyWishes().join()
                    }
                    navigator.navigate(WishDetailedRoute.fromSharedDraft(draft))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        extractSharedDraft(intent)?.let {
            lifecycleScope.launch {
                sharedDraftFlow.emit(it)
            }
        }
    }

    /**
     * Текст из «Поделиться» другого приложения разбирается в `shared`: адреса идут в ссылки,
     * остальное в название и комментарий. Пустой текст игнорируется.
     */
    private fun extractSharedDraft(intent: Intent?): SharedWishDraft? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND || intent.type != "text/plain") return null

        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        return SharedTextParser.parse(subject = subject, text = text).takeUnless { it.isEmpty }
    }
}
