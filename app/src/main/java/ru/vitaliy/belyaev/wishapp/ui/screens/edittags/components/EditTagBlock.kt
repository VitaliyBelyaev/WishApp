package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun EditTagBlock(
    editTagItem: EditTagItem,
    onClick: (TagEntity) -> Unit,
    onRemoveClick: (TagEntity) -> Unit,
    onEditDoneClick: (String, TagEntity) -> Unit,
) {
    val isEditMode = editTagItem.isEditMode
    val tag = editTagItem.tag

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable {
                if (!isEditMode) {
                    onClick(tag)
                }
            }
    ) {
        val (startIconRef, titleRef, endIconRef, topDividerRef, bottomDividerRef) = createRefs()

        val dividerColor: Color = if (isEditMode) {
            MaterialTheme.colorScheme.secondary
        } else {
            Color.Transparent
        }
        val dividerThickness = 1.5.dp
        Divider(
            color = dividerColor,
            thickness = dividerThickness,
            modifier = Modifier
                .constrainAs(topDividerRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
        Divider(
            color = dividerColor,
            thickness = dividerThickness,
            modifier = Modifier
                .constrainAs(bottomDividerRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        if (isEditMode) {
            IconButton(
                onClick = { onRemoveClick(tag) },
                modifier = Modifier
                    .constrainAs(startIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 4.dp)
                    }
            ) {
                ThemedIcon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            var textFieldValue: TextFieldValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = tag.title,
                        selection = TextRange(tag.title.length),
                        composition = TextRange(0, tag.title.length)
                    )
                )
            }

            val focusRequester = remember { FocusRequester() }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .constrainAs(titleRef) {
                        width = Dimension.fillToConstraints
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(startIconRef.end)
                        end.linkTo(endIconRef.start)
                    },
                value = textFieldValue,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEditDoneClick(textFieldValue.text, editTagItem.tag)
                    }
                ),
                onValueChange = { newValue -> textFieldValue = newValue },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
            )
            DisposableEffect(key1 = Unit) {
                focusRequester.requestFocus()
                onDispose { }
            }
            IconButton(
                onClick = { onEditDoneClick(textFieldValue.text, editTagItem.tag) },
                enabled = textFieldValue.text.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .constrainAs(endIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 4.dp)
                    }
            ) {
                ThemedIcon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = "Done"
                )
            }
        } else {
            ThemedIcon(
                painterResource(R.drawable.ic_label),
                contentDescription = "Label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .constrainAs(startIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
            )

            Text(
                text = tag.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(titleRef) {
                        width = Dimension.fillToConstraints
                        start.linkTo(startIconRef.end, margin = 28.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(endIconRef.start, margin = 16.dp)
                    }
            )

            IconButton(
                onClick = { onClick(tag) },
                modifier = Modifier
                    .constrainAs(endIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 4.dp)
                    }
            ) {
                ThemedIcon(
                    painterResource(R.drawable.ic_edit),
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}