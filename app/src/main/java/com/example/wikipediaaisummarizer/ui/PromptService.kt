package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    fun getPrompt(content: String?) = "Write the response in " +
            "${Locale.getDefault().getDisplayLanguage(Locale("en"))}. " +
            "1. Explain what the subject is known for.\n" +
            "2. Describe the most interesting thing about the subject in the text below.\n" +
            "3. If there’s a notable story connected to the subject, tell it.\n" +
            "4. What’s the most meme-worthy or funniest aspect of the subject, if any?\n" +
            "5. Feel free to include any additional information about the subject (or related stories) beyond the provided text if you know it.\n" +
            "Here’s the text: $content"

    fun getPromptWithTopicName(topic: String?) = "Write the response in " +
            "${Locale.getDefault().getDisplayLanguage(Locale("en"))}." +
            "Tell me about $topic\n" +
            "1. Explain what the subject is known for.\n" +
            "2. Describe the most interesting thing about the subject.\n" +
            "3. If there’s a notable story connected to the subject, tell it.\n" +
            "4. What’s the most meme-worthy or funniest aspect of the subject, if any?\n" +
            "5. Feel free to include any additional information about the subject (or related stories) if you know it.\n"
}