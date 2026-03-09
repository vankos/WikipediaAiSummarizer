// WikiSummarizerApp.kt
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wikipediaaisummarizer.ui.PromptService
import com.yourpackage.wikisummarizer.network.WikipediaApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets



@Composable
fun WikiSummarizerApp(incomingLink: String = "") {
    val context = LocalContext.current
    var wikiLink by remember { mutableStateOf("") }
    val promptService = PromptService()

    LaunchedEffect(incomingLink) {
        if (incomingLink.isNotEmpty()) {
            wikiLink = incomingLink
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

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val uri = Uri.parse(wikiLink)
                val title = GetTitleFromUrl(uri)
                val prompt = promptService.getPromptWithTopicName(title)
                clipboardManager.setText(AnnotatedString(prompt))
                Toast.makeText(context, "Prompt copied — paste it in Gemini", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://gemini.google.com/app")))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open in Gemini")
        }

        Button(
            onClick = { openPromptInAI(context, "https://claude.ai/new?q=", wikiLink, promptService) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open in Claude")
        }

        Button(
            onClick = { openPromptInAI(context, "https://chatgpt.com/?q=", wikiLink, promptService) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open in ChatGPT")
        }
    }
}

private fun openPromptInAI(context: android.content.Context, baseUrl: String, wikiLink: String, promptService: PromptService) {
    val uri = Uri.parse(wikiLink)
    val title = GetTitleFromUrl(uri)
    val prompt = promptService.getPromptWithTopicName(title)
    val encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.name())
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$baseUrl$encodedPrompt"))
    context.startActivity(intent)
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