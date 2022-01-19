package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
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
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem

@ExperimentalComposeUiApi
@Composable
fun EditTagBlock(
    editTagItem: EditTagItem,
    onClick: (Tag) -> Unit,
    onRemoveClick: (Tag) -> Unit,
    onEditDoneClick: (String) -> Unit,
    onEditingItemFocusRequested: () -> Unit,
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
            MaterialTheme.colors.primary
        } else {
            Color.Transparent
        }
        Divider(
            color = dividerColor,
            modifier = Modifier
                .constrainAs(topDividerRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
        Divider(
            color = dividerColor,
            modifier = Modifier
                .constrainAs(bottomDividerRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        val focusRequester = remember { FocusRequester() }
        if (isEditMode) {
            DisposableEffect(isEditMode) {
                focusRequester.requestFocus()
                onEditingItemFocusRequested()
                onDispose { }
            }
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
                    contentDescription = "Delete"
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
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
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
                textStyle = MaterialTheme.typography.body1,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEditDoneClick(textFieldValue.text)
                    }
                ),
                onValueChange = { newValue -> textFieldValue = newValue },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            val doneIconTint: Color = if (textFieldValue.text.isNotBlank()) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.primary.copy(alpha = 0.5f)
            }
            IconButton(
                onClick = { onEditDoneClick(textFieldValue.text) },
                enabled = textFieldValue.text.isNotBlank(),
                modifier = Modifier
                    .constrainAs(endIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 4.dp)
                    }
            ) {
                ThemedIcon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = "Done",
                    tint = doneIconTint,
                )
            }
        } else {
            ThemedIcon(
                painterResource(R.drawable.ic_label),
                contentDescription = "Label",
                modifier = Modifier
                    .constrainAs(startIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
            )
            Text(
                text = tag.title,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground,
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
                )
            }
        }
    }
}