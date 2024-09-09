package com.plasmavan.tinytutor

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat.getString

data class NavigationItemData(
    val context: Context,
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = ""
) {
    fun bottomNavigationItems() : ArrayList<NavigationItemData> {
        return arrayListOf(
            NavigationItemData(
                context = context,
                label = getString(context, R.string.template_navigation_title),
                icon = Icons.Filled.Create,
                route = ComposeObject.TemplatePromptUI.route
            ),
            NavigationItemData(
                context = context,
                label = getString(context, R.string.manual_navigation_title),
                icon = Icons.Filled.Edit,
                route = ComposeObject.ManualPromptUI.route
            ),
            NavigationItemData(context = context,

                label = getString(context, R.string.saved_navigation_title),
                icon = Icons.Filled.Star,
                route = ComposeObject.SavedQuestionsUI.route
            )
        )
    }
}
