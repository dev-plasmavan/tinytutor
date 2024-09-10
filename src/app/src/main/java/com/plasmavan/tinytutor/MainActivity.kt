package com.plasmavan.tinytutor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plasmavan.tinytutor.database.AppViewModel
import com.plasmavan.tinytutor.database.SavedQuestions
import com.plasmavan.tinytutor.ui.theme.TinyTutorTheme
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()
    private var savedQuestionsList: List<SavedQuestions> = listOf()
    private var loading by mutableStateOf(true)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fieldList: ArrayList<String> = createArrayListFromCsv("fields.csv")
        val levelList: ArrayList<String> = createArrayListFromCsv("levels.csv")
        val difficultyList: ArrayList<String> = createArrayListFromCsv("difficulty.csv")
        val certificationList: ArrayList<String> = createArrayListFromCsv("certifications.csv")

        enableEdgeToEdge()
        setContent {
            TinyTutorTheme {
                var navigationSelectedItem by remember { mutableIntStateOf(0) }
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text( text = getString(R.string.app_name) ) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            actions = {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(applicationContext, InfoActivity::class.java)
                                        startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "About this app",
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationItemData(context = applicationContext).bottomNavigationItems().forEachIndexed { index, navigationItemData ->
                                NavigationBarItem(
                                    selected = index == navigationSelectedItem,
                                    label = { Text( navigationItemData.label ) },
                                    icon = { Icon(
                                        navigationItemData.icon,
                                        contentDescription = navigationItemData.label
                                    ) },
                                    onClick = {
                                        navigationSelectedItem = index
                                        navController.navigate(navigationItemData.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = false
                                            }
                                            launchSingleTop = true
                                            restoreState = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = ComposeObject.TemplatePromptUI.route,
                        modifier = Modifier.padding( paddingValues = innerPadding )
                    ) {
                        composable(ComposeObject.TemplatePromptUI.route) {
                            TemplatePromptUI(fieldList, levelList, difficultyList, certificationList)
                        }
                        composable(ComposeObject.ManualPromptUI.route) {
                            ManualPromptUI()
                        }
                        composable(ComposeObject.SavedQuestionsUI.route) {
                            SavedQuestionsUI()
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            super.onKeyDown(keyCode, event)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun createArrayListFromCsv(fileName: String) : ArrayList<String> {
        return try {
            val arrayList: ArrayList<String> = arrayListOf()
            val csvFile = resources.assets.open(fileName)
            val csvReader = BufferedReader(InputStreamReader(csvFile))

            csvReader.forEachLine {
                arrayList.add(it)
            }

            arrayList
        } catch (e: Exception) {
            Log.e("Error", "$e")
            arrayListOf()
        }
    }

    private fun getSavedQuestions(onDataLoaded: (List<SavedQuestions>) -> Unit) {
        appViewModel.selectAllQuestions().observe(this@MainActivity) {
            onDataLoaded(it)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TemplatePromptUI(
        fieldList: ArrayList<String>,
        levelList: ArrayList<String>,
        difficultyList: ArrayList<String>,
        certificationList: ArrayList<String>
    ) {
        var fieldText by remember { mutableStateOf(fieldList[0]) }
        var levelText by remember { mutableStateOf(levelList[0]) }
        var difficultyText by remember { mutableStateOf(difficultyList[0]) }
        var certificationsText by remember { mutableStateOf(certificationList[0]) }
        var expandedField by remember { mutableStateOf(false) }
        var expandedLevel by remember { mutableStateOf(false) }
        var expandedDifficulty by remember { mutableStateOf(false) }
        var expandedCertifications by remember { mutableStateOf(false) }
        val customState: Boolean = false

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = getString(R.string.template_prompt_header_text)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(3F),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(
                                    text = getString(R.string.field_selector),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = expandedField,
                                    onExpandedChange = {
                                        expandedField = !expandedField
                                    }
                                ) {
                                    TextField(
                                        value = fieldText,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedField) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedField,
                                        onDismissRequest = { expandedField = false }
                                    ) {
                                        fieldList.forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text(text = item) },
                                                onClick = {
                                                    fieldText = item
                                                    expandedField = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(
                                    text = getString(R.string.level_selector),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = expandedLevel,
                                    onExpandedChange = {
                                        expandedLevel = !expandedLevel
                                    }
                                ) {
                                    TextField(
                                        value = levelText,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLevel) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedLevel,
                                        onDismissRequest = { expandedLevel = false }
                                    ) {
                                        levelList.forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text(text = item) },
                                                onClick = {
                                                    levelText = item
                                                    expandedLevel = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(
                                    text = getString(R.string.difficulty_selector),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = expandedDifficulty,
                                    onExpandedChange = {
                                        expandedDifficulty = !expandedDifficulty
                                    }
                                ) {
                                    TextField(
                                        value = difficultyText,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDifficulty) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedDifficulty,
                                        onDismissRequest = { expandedDifficulty = false }
                                    ) {
                                        difficultyList.forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text(text = item) },
                                                onClick = {
                                                    difficultyText = item
                                                    expandedDifficulty = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(
                                    text = getString(R.string.certifications_selector),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = expandedCertifications,
                                    onExpandedChange = {
                                        expandedCertifications = !expandedCertifications
                                    }
                                ) {
                                    TextField(
                                        value = certificationsText,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCertifications) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedCertifications,
                                        onDismissRequest = { expandedCertifications = false }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 200.dp, height = 600.dp)
                                        ) {
                                            LazyColumn {
                                                items(certificationList) { item ->
                                                    DropdownMenuItem(
                                                        text = { Text(text = item) },
                                                        onClick = {
                                                            certificationsText = item
                                                            expandedCertifications = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if(fieldText == "なし" && levelText == "なし" && difficultyText == "なし" && certificationsText == "なし") {
                                Toast.makeText(applicationContext, getString(R.string.requirement_prompt), Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val intent = Intent(applicationContext, ResultActivity::class.java)
                            intent.putExtra("field", fieldText)
                            intent.putExtra("level", levelText)
                            intent.putExtra("difficulty", difficultyText)
                            intent.putExtra("certification", certificationsText)
                            intent.putExtra("custom_state", customState)

                            startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Make questions.",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ManualPromptUI() {
        var inputPrompt by rememberSaveable { mutableStateOf("") }
        val customState: Boolean = true

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = getString(R.string.manual_prompt_header_text)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(3F)
                        .padding(20.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text(
                            text = getString(R.string.custom_prompt_input_field),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                        TextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if(inputPrompt == "") {
                                Toast.makeText(applicationContext, getString(R.string.requirement_input), Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val intent = Intent(applicationContext, ResultActivity::class.java)
                            intent.putExtra("field", "なし")
                            intent.putExtra("level", "なし")
                            intent.putExtra("difficulty", "なし")
                            intent.putExtra("certification", "なし")
                            intent.putExtra("custom_prompt", inputPrompt)
                            intent.putExtra("custom_state", customState)

                            startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Make questions.",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SavedQuestionsUI() {
        val openDialog = remember { mutableStateOf(false) }
        val questionToDelete = remember { mutableStateOf<SavedQuestions?>(null) }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = { openDialog.value = false },
                title = { Text(text = getString(R.string.confirm_deletion)) },
                text = { Text(text = getString(R.string.confirm_deletion_content)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            questionToDelete.value?.let {
                                appViewModel.deleteOne(it.id)
                                loading = true
                            }
                            openDialog.value = false
                        }
                    ) {
                        Text(getString(R.string.delete_button))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { openDialog.value = false }
                    ) {
                        Text(getString(R.string.cancel_button))
                    }
                }
            )
        }

        getSavedQuestions {
            savedQuestionsList = it
        }

        var searchText by remember { mutableStateOf("") }
        var filteredList: List<SavedQuestions> = listOf()
        if(savedQuestionsList.isNotEmpty()) {
            filteredList = savedQuestionsList.filter { it.savedContent!!.contains(searchText, ignoreCase = true) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            LazyColumn {
                items(filteredList) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            Text(
                                text = it.createdDate.toString(),
                                modifier = Modifier
                                    .padding(8.dp)
                            )

                            if(it.contentField != null && it.contentLevel != null && it.contentDifficulty != null && it.contentCertification != null) {
                                Text(
                                    text = "${it.contentField}, ${it.contentLevel}, ${it.contentDifficulty}, ${it.contentCertification}",
                                    modifier = Modifier
                                        .padding(8.dp)
                                )
                            }

                            ElevatedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    val intent = Intent(applicationContext, QuestionActivity::class.java)
                                    intent.putExtra("saved_question", it.savedContent)

                                    startActivity(intent)
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(4F)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = it.contentTitle.toString(),
                                            textAlign = TextAlign.Start,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1F)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        IconButton(
                                            onClick = {
                                                questionToDelete.value = it
                                                openDialog.value = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(ButtonDefaults.IconSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}