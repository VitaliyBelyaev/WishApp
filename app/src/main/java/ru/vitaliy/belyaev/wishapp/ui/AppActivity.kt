package ru.vitaliy.belyaev.wishapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.theme.WishAppTheme

@ExperimentalMaterialApi
@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val viewModel: AppActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.wishListToShareLiveData.observe(this) {
            val wishListAsFormattedText = generateFormattedWishList(it)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, wishListAsFormattedText)
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        viewModel.requestReviewLiveData.observe(this) {
            Firebase.analytics.logEvent("in_app_review_requested", null)
            val reviewManager = ReviewManagerFactory.create(this)
            reviewManager
                .requestReviewFlow()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        Firebase.analytics.logEvent("in_app_review_shown", null)
                        reviewManager.launchReviewFlow(this, reviewInfo)
                    } else {
                        task.exception?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                    }
                }
        }

        setContent {
            val selectedTheme: Theme by viewModel.selectedTheme.collectAsState()
            val modeInt = when (selectedTheme) {
                Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            }
            // We need to change theme in Activity too for proper colors that we get from resources
            AppCompatDelegate.setDefaultNightMode(modeInt)
            WishAppTheme(selectedTheme = selectedTheme) {
                Surface(color = MaterialTheme.colors.background) {
                    Navigation { viewModel.onShareWishListClicked(it) }
                }
            }
        }
    }

    private fun generateFormattedWishList(wishes: List<WishWithTags>): String {
        val builder = StringBuilder().apply {
            append(getString(R.string.wish_list_title))
            append("\n\n")
        }

        wishes.forEachIndexed { index, wish ->
            val number = index + 1
            builder.append("$number. ${wish.title}\n")
            if (wish.comment.isNotBlank()) {
                builder.append("\n")
                builder.append("${wish.comment}\n")
            }
            if (wish.link.isNotBlank()) {
                builder.append("\n")
                builder.append("${wish.link}\n")
            }
            if (index != wishes.lastIndex) {
                builder.append("_____________")
                builder.append("\n\n")
            }
        }
        return builder.toString()
    }
}