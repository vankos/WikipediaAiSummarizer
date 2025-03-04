package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    fun getPrompt(content: String?) = "Write the response in " +
            "${Locale.getDefault().getDisplayLanguage(Locale("en"))}. " +
            "1. Describe the most interesting thing about the subject in the text below.\n" +
            "2. Explain what the subject is known for.\n" +
            "3. If there’s a notable story connected to the subject, tell it.\n" +
            "4. Feel free to include any additional information about the subject (or related stories) beyond the provided text if you know it.\n" +
            "5. Provide a good source about the subject (besides a Wikipedia article) if available—a book, film, or article is fine.\n" +
            "Here’s the text: $content"

    fun getPromptWithTopicName(topic: String?) = "Write the response in " +
            "${Locale.getDefault().getDisplayLanguage(Locale("en"))}." +
            "Tell me about $topic\n" +
            "1. Describe the most interesting thing about it.\n" +
            "2. Explain what the subject is known for.\n" +
            "3. If there’s a notable story connected to the subject, tell it.\n" +
            "4. Feel free to include any additional information about the subject (or related stories) beyond the provided text if you know it.\n" +
            "5. Provide a good source about the subject (besides a Wikipedia article) if available—a book, film, or article is fine."

}