package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Правила `QuickAddSheet`: когда можно создать, что делает ярлык фильтра и поле ссылки. */
class NewWishDraftTest {

    @Test
    fun canCreate_needsTitleCommentLinkOrPhoto_notSpacesOrTag() {
        assertFalse(NewWishDraft(title = "   ", tagIds = listOf("t")).canCreate)
        assertTrue(NewWishDraft(title = "Лампа").canCreate)
        assertTrue(NewWishDraft(comment = "тёплая").canCreate)
        assertTrue(NewWishDraft(links = listOf("https://a.example")).canCreate)
        assertTrue(NewWishDraft(images = listOf(DraftImage("1", byteArrayOf(1)))).canCreate)
    }

    @Test
    fun openedInTag_emptyDraftStartsWithThatTag() {
        val draft = NewWishDraft().openedIn(contextTagId = "home", title = null)

        assertEquals(listOf("home"), draft.tagIds)
    }

    @Test
    fun openedInOtherTag_draftWithContentKeepsItsTags() {
        val draft = NewWishDraft(
            title = "Лампа",
            tagIds = listOf("garden"),
        ).openedIn(contextTagId = "home", title = null)

        assertEquals(listOf("garden"), draft.tagIds)
        assertEquals("Лампа", draft.title)
    }

    @Test
    fun openedFromSearch_titleGoesToEmptyDraftOnly() {
        assertEquals("наушники", NewWishDraft().openedIn(null, "наушники").title)
        assertEquals("Лампа", NewWishDraft(title = "Лампа").openedIn(null, "наушники").title)
    }

    @Test
    fun linkInputDone_addsBareDomainWithoutDuplicates() {
        val draft = NewWishDraft(links = listOf("https://ozon.ru/a"), linkInput = " ozon.ru/a ")
            .edited(NewWishAction.LinkInputDone)

        assertEquals(listOf("https://ozon.ru/a"), draft.links)
        assertNull(draft.linkInput)
        assertFalse(draft.isLinkInvalid)
    }

    @Test
    fun linkInputDone_notALink_keepsFieldWithError() {
        val draft = NewWishDraft(linkInput = "наушники").edited(NewWishAction.LinkInputDone)

        assertEquals("наушники", draft.linkInput)
        assertTrue(draft.isLinkInvalid)
        assertTrue(draft.links.isEmpty())
    }

    @Test
    fun pastedText_takesOnlyAddresses() {
        val draft = NewWishDraft().withPastedLinks("Смотри https://a.example/x, классно")

        assertEquals(listOf("https://a.example/x"), draft?.links)
    }

    @Test
    fun tagToggle_firstSelectedStaysFirst() {
        val draft = NewWishDraft()
            .edited(NewWishAction.TagToggled("a"))
            .edited(NewWishAction.TagToggled("b"))
            .edited(NewWishAction.TagToggled("a"))
            .edited(NewWishAction.TagToggled("c"))

        assertEquals(listOf("b", "c"), draft.tagIds)
    }

    @Test
    fun clearable_whenOnlyTagOrOpenedComment() {
        assertFalse(NewWishDraft().isClearable)
        assertTrue(NewWishDraft(tagIds = listOf("a")).isClearable)
        assertTrue(NewWishDraft(comment = "").isClearable)
    }

    @Test
    fun untouchedDraftInTag_isNotClearableAndFollowsNextFilter() {
        val inHome = NewWishDraft.fresh("home")
        assertFalse(inHome.isClearable)

        // Закрыли, ничего не написав, и открыли в «Хотелках»: ярлык «home» не тянется следом
        assertTrue(inHome.openedIn(contextTagId = null, title = null).tagIds.isEmpty())
        // А убранный вручную подставленный ярлык — уже правка
        assertTrue(inHome.edited(NewWishAction.TagToggled("home")).isClearable)
    }
}
