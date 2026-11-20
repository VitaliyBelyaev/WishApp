package ru.vitaliy.belyaev.wishapp.ui.components.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.pageTitle

/**
 * Плавающая шапка вложенных экранов нового вида: настройки, дальше резервная копия, ярлыки,
 * «О приложении». Выбрана владельцем 19 ноября 2026 из трёх вариантов.
 *
 * «Назад» в круглой капсуле, как кнопки на главной: в начале экрана прямо на фоне, с прокруткой
 * под ним проявляется `surface-card` с тенью. Заголовок обычного размера ([pageTitle]), крупный
 * Unbounded только у разделов списка. При прокрутке заголовок уходит вверх и гаснет, как на главной,
 * и возвращается, только когда экран прокручен обратно до верха.
 *
 * Экран под шапкой прокручивается целиком и начинается ниже неё: отступ сверху [headerHeight].
 *
 * @param progress от 0 (начало экрана) до 1 (прокручено на [PageHeaderCollapse] и больше).
 * @param action кнопка справа, например «i» у резервной копии. Капсула под ней проявляется вместе с «назад».
 */
@Composable
fun PageHeader(
    title: String,
    progress: () -> Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        HeaderScrim(progress)
        Row(
            modifier = Modifier
                .windowInsetsPadding(HeaderInsets)
                .fillMaxWidth()
                .height(HeaderRowHeight)
                .padding(horizontal = WishAppSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            Box {
                CapsuleBackdrop(progress)
                IconButton(onClick = onBack, modifier = Modifier.size(HeaderButtonSize)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_arrow_back),
                        contentDescription = stringResource(R.string.cd_back),
                    )
                }
            }
            val shift = with(LocalDensity.current) { TitleShift.toPx() }
            Text(
                text = title,
                style = MaterialTheme.typography.pageTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
                    .graphicsLayer {
                        val p = progress()
                        alpha = (1f - p * 2.5f).coerceIn(0f, 1f)
                        translationY = -shift * p
                    },
            )
            if (action != null) {
                Box {
                    CapsuleBackdrop(progress)
                    action()
                }
            }
        }
    }
}

/** Сколько прокрутить, чтобы заголовок ушёл, а под «назад» проявилась капсула. Как на главной. */
val PageHeaderCollapse: Dp = 72.dp

/** На сколько заголовок уезжает вверх, пока гаснет. */
private val TitleShift: Dp = 14.dp
