package com.plasmavan.tinytutor

import android.content.res.Resources
import android.net.wifi.WifiManager.SubsystemRestartTrackingCallback
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.plasmavan.tinytutor.database.AppViewModel
import com.plasmavan.tinytutor.database.SavedQuestions
import com.plasmavan.tinytutor.ui.theme.TinyTutorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ResultActivity : ComponentActivity() {

    private var currentDate: String? = null
    private var loading by mutableStateOf(true)
    private val appViewModel: AppViewModel by viewModels()

    init {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar[Calendar.YEAR]
        val month: Int = calendar[Calendar.MONTH]
        val day: Int = calendar[Calendar.DAY_OF_MONTH]
        val hour: Int = calendar[Calendar.HOUR_OF_DAY]
        val minute: Int = calendar[Calendar.MINUTE]
        val second: Int = calendar[Calendar.SECOND]

        currentDate = "${year}-${month}-${day} ${hour}:${minute}:${second}"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fieldText: String = intent.getStringExtra("field").toString()
        val levelText: String = intent.getStringExtra("level").toString()
        val difficultyText: String = intent.getStringExtra("difficulty").toString()
        val certificationsText: String = intent.getStringExtra("certification").toString()

        val customPrompt: String = intent.getStringExtra("custom_prompt").toString()
        val customState: Boolean = intent.getBooleanExtra("custom_state", false)

        val systemLanguageState: String = Resources.getSystem().configuration.locale.language

        val prompt: String =
            if (systemLanguageState == "ja" && !customState) {
                "四択問題を以下の内容かつ以下の形式で作成してください。また、内容が「なし」のものについては無視し、最低でも１０問は作成してください。もし、問題がこの内容で問題が作成できない場合には、おすすめの問題を提案してください。" +
                        "\n# 内容\n- 分野：${fieldText}\n- レベル：${levelText}\n- 難易度：${difficultyText}\n- 関連する検定・資格：${certificationsText}" +
                        "\n# 形式の例（カッコ内はその部分の内容である）\n問題(問題番号)\n(問題文)\n問１：(解答の選択1)\n問２：(解答の選択2)\n問３：(解答の選択3)\n問４：(解答の選択4)\n解答：(この問題の解答)"

            } else if (systemLanguageState != "ja" && !customState) {
                "Please create four (4) questions with the following content and in the following format. Also, please ignore those with “none” content and create at least 10 questions. If a question cannot be created with this content, please suggest a recommended question." +
                        "\n# Contents\n- Field: ${fieldText}\n- Level: ${levelText}\n- Difficulty: ${difficultyText}\n- - Related Certifications/Certifications: ${certificationsText}" +
                        "\n# Example of format (the part in parentheses is the content of the section)\nQuestion (Question number)\n(Question text)\nQ1: (Answer choice 1)\nQ2: (Answer choice 2)\nQ3: (Answer choice 3)\nQ4: (Answer choice 4)\nSolution: (Answer to this question)"
            } else if (systemLanguageState == "ja" && customState) {
                "四択問題を以下の内容かつ以下の形式で作成してください。また、内容が「なし」のものについては無視し、最低でも１０問は作成してください。もし、問題がこの内容で問題が作成できない場合には、おすすめの問題を提案してください。" +
                        "\n# 内容\n${customPrompt}" +
                        "\n# 形式の例（カッコ内はその部分の内容である）\n問題(問題番号)\n(問題文)\n問１：(解答の選択1)\n問２：(解答の選択2)\n問３：(解答の選択3)\n問４：(解答の選択4)\n解答：(この問題の解答)"

            } else {
                "Please create four (4) questions with the following content and in the following format. Also, please ignore those with “none” content and create at least 10 questions. If a question cannot be created with this content, please suggest a recommended question." +
                        "\n# Contents\n${customPrompt}" +
                        "\n# Example of format (the part in parentheses is the content of the section)\nQuestion (Question number)\n(Question text)\nQ1: (Answer choice 1)\nQ2: (Answer choice 2)\nQ3: (Answer choice 3)\nQ4: (Answer choice 4)\nSolution: (Answer to this question)"
            }.toString()

        var response by mutableStateOf("")

        generateResponse(prompt) {
            response = it
        }

        enableEdgeToEdge()
        setContent {
            TinyTutorTheme {
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
                            navigationIcon = {
                                IconButton( onClick = { finish() } ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Back",
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        saveTheQuestion(currentDate.toString(), response, fieldText, levelText, difficultyText, certificationsText)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        if(loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        text = getString(R.string.making_questions),
                                        textAlign = TextAlign.Center
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier,
                                            color = MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        )
                                    }
                                }
                            }
                        } else {
                            AnswerTheQuizUI(response)
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    private fun generateResponse(prompt: String, onResponseReceived: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = BuildConfig.apiKey
            )

            val response: String = generativeModel.generateContent(prompt).text.toString()

            withContext(Dispatchers.Main) {
                onResponseReceived(response)
                loading = false
            }
        }
    }

    private fun saveTheQuestion(
        date: String,
        content: String,
        field: String,
        level: String,
        difficulty: String,
        certification: String
    ) {
        try {
            val title: String = content.substringBefore("\n")

            appViewModel.insertTheQuestion(
                date = date,
                title = title,
                content = content,
                field = field,
                level = level,
                difficulty = difficulty,
                certification = certification
            )

            Toast.makeText(applicationContext, getString(R.string.saved_questions), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Error", "$e")
            Toast.makeText(applicationContext, getString(R.string.error_save_questions), Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun AnswerTheQuizUI(
        quiz: String
    ) {
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
                    .padding(32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = quiz,
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Start,
                    )
                }
            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                contentAlignment = Alignment.TopStart
//            ) {
//                Column {
//                    optionList.forEach {
//                        Row(
//                            modifier = Modifier.padding(8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            RadioButton(
//                                selected = selectedOption == it,
//                                onClick = { selectedOption = it }
//                            )
//                            Text(
//                                text = it,
//                                style = MaterialTheme.typography.bodyLarge,
//                                modifier = Modifier.padding(start = 8.dp)
//                            )
//                        }
//                    }
//                }
//            }
        }
    }
}