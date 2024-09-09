package com.plasmavan.tinytutor

sealed class ComposeObject(val route: String) {
    data object TemplatePromptUI: ComposeObject("template")
    data object ManualPromptUI: ComposeObject("manual")
    data object SavedQuestionsUI: ComposeObject("saved")
}