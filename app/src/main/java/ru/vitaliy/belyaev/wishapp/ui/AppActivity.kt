package ru.vitaliy.belyaev.wishapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import dagger.hilt.android.AndroidEntryPoint
import ru.vitaliy.belyaev.model.database.Wish
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.theme.WishAppTheme

@ExperimentalMaterialApi
@AndroidEntryPoint
class AppActivity : ComponentActivity() {

    private val viewModel: AppActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.wishListLiveData.observe(this) {
            val wishListAsFormattedText = generateFormattedWishList(it)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, wishListAsFormattedText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        setContent {
            WishAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Navigation { viewModel.requestWishListAsText() }
                }
            }
        }
    }

    private fun generateFormattedWishList(wishes: List<Wish>): String {
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