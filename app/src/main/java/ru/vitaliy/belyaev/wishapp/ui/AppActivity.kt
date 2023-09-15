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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
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
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.domain.ShareWishListTextGenerator
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppTheme
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import timber.log.Timber

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

        if (savedInstanceState == null) {
            sharedLinkFromAnotherApp = extractSharedLinkAndShowErrorIfInvalid(intent)
        }

        lifecycleScope.launch {
            viewModel.wishListToShareFlow.collect {
                val wishListAsFormattedText = ShareWishListTextGenerator.generateFormattedWishListText(
                    title = getString(R.string.wish_list_title),
                    wishes = it
                )
                startActivity(createSharePlainTextIntent(wishListAsFormattedText))
            }
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
            val selectedTheme: Theme by viewModel.selectedTheme.collectAsState()
            val navController = rememberAnimatedNavController()
            WishAppTheme(selectedTheme = selectedTheme) {
                Navigation(
                    navController = navController,
                    onShareClick = { viewModel.onShareWishListClicked(it) },
                    analyticsRepository = analyticsRepository,
                )
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