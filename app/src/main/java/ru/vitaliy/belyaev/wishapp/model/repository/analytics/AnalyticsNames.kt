package ru.vitaliy.belyaev.wishapp.model.repository.analytics

object AnalyticsNames {

    object Event {
        const val SCREEN_VIEW = "screen_view"
        const val IN_APP_REVIEW_REQUESTED = "in_app_review_requested"
        const val IN_APP_REVIEW_SHOWN = "in_app_review_shown"
        const val WISH_LINK_CLICK = "wish_link_click"
        const val DELETE_TAG_FROM_WISH_DETAILED = "delete_tag_from_wish_detailed"
        const val CLOSE_EDIT_MODE_CLICK = "close_edit_mode_click"
        const val DELETE_FROM_EDIT_MODE_CLICK = "delete_from_edit_mode_click"
        const val SELECT_ALL_WISHES_PRESS = "select_all_wishes_press"
        const val WISH_LONG_PRESS = "wish_long_press"
        const val FILTER_BY_TAG_CLICK = "filter_by_tag_click"
        const val SHARE = "share"
        const val SEND_FEEDBACK_CLICK = "send_feedback_click"
        const val ADD_NEW_TAG = "add_new_tag"
        const val SELECT_APP_THEME = "select_app_theme"
        const val ABOUT_DATA_BACKUP_CLICK = "about_data_backup_click"
        const val RATE_APP_CLICK = "rate_app_click"
        const val SOURCE_CODE_URL_CLICK = "source_code_url_click"
        const val TAG_CLICKED_FROM_EDIT_TAGS = "tag_clicked_from_edit_tags"
        const val DELETE_TAG_FROM_EDIT_TAGS = "delete_tag_from_edit_tags"
        const val EDIT_TAG_DONE_CLICKED_FROM_EDIT_TAGS = "edit_tag_done_clicked_from_edit_tags"
    }

    object Param {
        const val SCREEN_NAME = "screen_name"
        const val QUANTITY = "quantity"
        const val THEME = "theme"
    }
}