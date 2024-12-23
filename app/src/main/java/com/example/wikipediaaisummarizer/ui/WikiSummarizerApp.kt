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
import java.util.Locale
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.yourpackage.wikisummarizer.network.WikipediaApiService
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


    LaunchedEffect(incomingLink) {
        if (incomingLink.isNotEmpty()) {
            wikiLink = incomingLink
            // Automatically trigger the summarization without button press
            if (apiKey.isNotEmpty()) {
                isLoading = true
                fetchAndSummarize(wikiLink, apiKey) { summary, error ->
                    isLoading = false
                    resultText = summary ?: "Error: $error"
                }
            }
        }
    }

    Column(modifier =
    Modifier.padding(16.dp, 40.dp, 16.dp, 16.dp)) {
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

        Button(
            onClick = {
                isLoading = true
                resultText = ""
                if (wikiLink.isNotEmpty() && apiKey.isNotEmpty()) {
                    fetchAndSummarize(wikiLink, apiKey) { summary, error ->
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

fun fetchAndSummarize(wikiLink: String, apiKey: String, callback: (String?, String?) -> Unit) {
    // Use Uri to parse the link
    val uri = Uri.parse(wikiLink)
    val titleEncoded = uri.lastPathSegment ?: ""
    val title = URLDecoder.decode(titleEncoded, StandardCharsets.UTF_8.name()).replace("_", " ")

    // Extract language code from the URL (default to "en" if not found)
    val languageCode = uri.host?.split(".")?.getOrNull(0) ?: "en"
    val baseUrl = "https://$languageCode.wikipedia.org/w/"

    // Create Retrofit instance with logging
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val wikiApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WikipediaApiService::class.java)

    val openAIService = createOpenAIService(apiKey)

    // Fetch article content
    val wikiCall = wikiApi.getArticleContent(
        titles = title
    )

    wikiCall.enqueue(object : Callback<WikiResponse> {
        override fun onResponse(call: Call<WikiResponse>, response: Response<WikiResponse>) {
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("WikiResponse", "Response body: $responseBody")

                val pages = responseBody?.query?.pages
                val page = pages?.values?.firstOrNull()
                val content = page?.extract

                if (!content.isNullOrEmpty()) {
                    // Now summarize using OpenAI API
                    val openAIRequest = OpenAIRequest(
                        model = "chatgpt-4o-latest",
                        messages = listOf(
                            Message(
                                role = "system",
                                content = "Summarize the text below in ${Locale.getDefault().getDisplayLanguage(Locale("en"))}." +
                                        "Write what the subject is known for." +
                                        "Highlight any cool or interesting facts as bullet points (if there aren’t any, just say so)." +
                                        "Avoid using formal Wikipedia-style language; keep it informal." +
                                        "If you can add more about the subject outside the provided text, include it in a separate section." +
                                        "Here's the text: $content"
                            )
                        )
                    )

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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WikiSummarizerApp()
}