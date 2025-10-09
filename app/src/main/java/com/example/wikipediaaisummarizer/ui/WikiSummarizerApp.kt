// WikiSummarizerApp.kt
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.wikipediaaisummarizer.ui.PromptService
import com.yourpackage.wikisummarizer.network.WikipediaApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object ApiKeyManager {
    private const val PREFS_NAME = "WikiSummarizerPrefs"
    private const val API_KEY_FIELD = "OpenAiApiKey"

    fun saveApiKey(context: android.content.Context, key: String) {
        val sharedPreferences =
            context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(API_KEY_FIELD, key)
            apply()
        }
    }

    fun getApiKey(context: android.content.Context): String? {
        val sharedPreferences =
            context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return sharedPreferences.getString(API_KEY_FIELD, null)
    }
}


@Composable
fun WikiSummarizerApp(incomingLink: String = "") {
    val context = LocalContext.current
    var wikiLink by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf(ApiKeyManager.getApiKey(context) ?: "") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val promptService =  PromptService()


    LaunchedEffect(incomingLink) {
        if (incomingLink.isNotEmpty()) {
            wikiLink = incomingLink
            // Automatically trigger the summarization without button press
            if (apiKey.isNotEmpty()) {
                isLoading = true
                fetchAndSummarize(wikiLink, apiKey, promptService) { summary, error ->
                    isLoading = false
                    resultText = summary ?: "Error: $error"
                }
            }
        }
    }

    Column(
        modifier =
        Modifier.padding(16.dp, 40.dp, 16.dp, 16.dp)
    ) {
        TextField(
            value = wikiLink,
            onValueChange = { wikiLink = it },
            label = { Text("Enter Wikipedia Link") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                ApiKeyManager.saveApiKey(context, it)
            },
            label = { Text("Enter OpenAI API Key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        val clipboardManager = LocalClipboardManager.current
        Button(
            onClick = {
                val wikiCall = getWikiRequest(wikiLink)
                wikiCall.enqueue(object : Callback<WikiResponse> {
                    override fun onResponse(call: Call<WikiResponse>, response: Response<WikiResponse>) {
                        if (response.isSuccessful) {
                            val content = getContentFromResponse(response)
                            if (content.isNullOrEmpty()){
                                Toast.makeText(context, "Wikipedia article content is empty", Toast.LENGTH_LONG).show()
                                return
                            }

                            val prompt = promptService.getPrompt(content)
                            clipboardManager.setText(AnnotatedString(prompt))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("WikiSummarizer", "HTTP ${response.code()}: $errorBody")
                            Toast.makeText(context, "Error ${response.code()}: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<WikiResponse>, t: Throwable) {
                        Toast.makeText(context, "Error: ${t.localizedMessage} ", Toast.LENGTH_LONG).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Copy prompt")
        }

        Button(
            onClick = {
                val uri = Uri.parse(wikiLink)
                val title = GetTitleFromUrl(uri)
                val prompt = promptService.getPromptWithTopicName(title)
                clipboardManager.setText(AnnotatedString(prompt))
                Toast.makeText(context, "Copied (title prompt)", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Copy prompt (title only)")
        }

        Button(
            onClick = {
                isLoading = true
                resultText = ""
                if (wikiLink.isNotEmpty() && apiKey.isNotEmpty()) {
                    fetchAndSummarize(wikiLink, apiKey, promptService) { summary, error ->
                        isLoading = false
                        resultText = summary ?: "Error: $error"
                    }
                } else {
                    isLoading = false
                    resultText = "Please enter both Wikipedia link and API key."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else "Generate Summary")
        }

        Spacer(modifier = Modifier.height(16.dp))
        val state = rememberScrollState()
        SelectionContainer()
        {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state)
                    .padding(12.dp),
                textAlign = TextAlign.Justify
            )
        }
    }
}

fun fetchAndSummarize(wikiLink: String, apiKey: String, promtService: PromptService, callback: (String?, String?) -> Unit) {
    val wikiCall = getWikiRequest(wikiLink)
    wikiCall.enqueue(object : Callback<WikiResponse> {
        override fun onResponse(call: Call<WikiResponse>, response: Response<WikiResponse>) {
            if (response.isSuccessful) {
                val content = getContentFromResponse(response)
                if (!content.isNullOrEmpty()) {
                    // Now summarize using OpenAI API
                    val openAIRequest = OpenAIRequest(
                        model = "chatgpt-4o-latest",
                        messages = listOf(
                            Message(
                                role = "system",
                                content = promtService.getPrompt(content)
                            )
                        )
                    )

                    val openAIService = createOpenAIService(apiKey)
                    val openAICall = openAIService.getSummary(openAIRequest)
                    openAICall.enqueue(object : Callback<OpenAIResponse> {
                        override fun onResponse(
                            call: Call<OpenAIResponse>,
                            response: Response<OpenAIResponse>
                        ) {
                            if (response.isSuccessful) {
                                val summary =
                                    response.body()?.choices?.firstOrNull()?.message?.content
                                callback(summary, null)
                            } else {
                                callback(
                                    null,
                                    "OpenAI API Error: ${response.errorBody()?.string()}"
                                )
                            }
                        }

                        override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                            callback(null, "OpenAI API Failure: ${t.localizedMessage}")
                        }
                    })
                } else {
                    callback(null, "Article content not found or is empty.")
                }
            } else {
                callback(null, "Wikipedia API Error: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<WikiResponse>, t: Throwable) {
            callback(null, "Wikipedia API Failure: ${t.localizedMessage}")
        }
    })
}

private fun getContentFromResponse(response: Response<WikiResponse>): String? {
    val responseBody = response.body()
    Log.d("WikiResponse", "Response body: $responseBody")

    val pages = responseBody?.query?.pages
    val page = pages?.values?.firstOrNull()
    val content = page?.extract
    return content
}

private fun getWikiRequest(wikiLink: String): Call<WikiResponse> {
    // Use Uri to parse the link
    val uri = Uri.parse(wikiLink)
    val title = GetTitleFromUrl(uri)
    // Extract language code from the URL (default to "en" if not found)
    val languageCode = uri.host?.split(".")?.getOrNull(0) ?: "en"
    val baseUrl = "https://$languageCode.wikipedia.org/w/"

    // Create Retrofit instance with logging
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "WikipediaAiSummarizer/1.0 (https://github.com/vankos/WikipediaAiSummarizer)")
                .build()
            chain.proceed(request)
        }
        .build()

    val wikiApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WikipediaApiService::class.java)

    // Fetch article content
    val wikiCall = wikiApi.getArticleContent(
        titles = title
    )
    return wikiCall
}

private fun GetTitleFromUrl(uri: Uri): String {
    val titleEncoded = uri.lastPathSegment ?: ""
    val title = URLDecoder.decode(titleEncoded, StandardCharsets.UTF_8.name()).replace("_", " ")
    return title
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WikiSummarizerApp()
}