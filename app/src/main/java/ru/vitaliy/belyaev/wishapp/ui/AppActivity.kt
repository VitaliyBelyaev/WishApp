package ru.vitaliy.belyaev.wishapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppReviewRequestedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppReviewShowEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.navigation.WishDetailedRoute
import ru.vitaliy.belyaev.wishapp.navigation.WishImagesViewerRoute
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
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

    private var sharedLinkFromAnotherApp: String? = null

    private val shareLinkFlow = MutableSharedFlow<String>()

    @ExperimentalAnimationApi
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        super.onCreate(savedInstanceState)


        if(savedInstanceState == null) {
            viewModel.onCreateWithoutSavedInstanceState(this)
        }


        if (savedInstanceState == null) {
            sharedLinkFromAnotherApp = extractSharedLinkAndShowErrorIfInvalid(intent)
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
            val forceDark: MutableState<Boolean> = remember { mutableStateOf(false) }
            val theme by remember {
                derivedStateOf {
                    if (forceDark.value) {
                        Theme.DARK
                    } else {
                        selectedTheme
                    }
                }
            }

            val navController = rememberNavController()
            WishAppTheme(selectedTheme = theme) {
                Navigation(
                    navController = navController,
                    analyticsRepository = analyticsRepository,
                )
            }
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val newForceDark = destination.route == WishImagesViewerRoute.VALUE
                if (newForceDark != forceDark.value) {
                    forceDark.value = newForceDark
                }
            }

            sharedLinkFromAnotherApp?.let {
                navController.navigate(WishDetailedRoute.buildRoute(wishLink = it))
                sharedLinkFromAnotherApp = null
            }

            LaunchedEffect(key1 = Unit) {
                shareLinkFlow.collect {
                    if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                        navController.popBackStack(navController.graph.startDestinationId, false)
                    }
                    navController.navigate(WishDetailedRoute.buildRoute(wishLink = it))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        extractSharedLinkAndShowErrorIfInvalid(intent)?.let {
            lifecycleScope.launch {
                shareLinkFlow.emit(it)
            }
        }
    }

    private fun extractSharedLinkAndShowErrorIfInvalid(intent: Intent?): String? {
        if (intent == null) return null

        val link = if (intent.action == Intent.ACTION_SEND &&
            intent.type == "text/plain" &&
            intent.hasExtra(Intent.EXTRA_TEXT)
        ) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        if (link == null) return null

        return if (validateLinkAndShowErrorIfInvalid(link)) link else null
    }

    private fun validateLinkAndShowErrorIfInvalid(link: String): Boolean {
        if (!isLinkValid(link)) {
            lifecycleScope.launch { viewModel.showSnackMessageOnMain(getString(R.string.invalid_link_error_message)) }
            return false
        }
        return true
    }

    private fun isLinkValid(link: String): Boolean {
        return Patterns.WEB_URL.matcher(link).matches()
    }
}