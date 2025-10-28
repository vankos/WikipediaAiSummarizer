package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    val promptTemplate : String = "\"Write the response in \" +\n" +
            "            \"${Locale.getDefault().getDisplayLanguage(Locale("en"))}. \" +\n" +
            "            \"1. Explain what the subject is known for.\\n\" +\n" +
            "            \"2. Describe the most interesting thing about the subject.\\n\" +\n" +
            "            \"3. If there’s a notable story connected to the subject, tell it.\\n\" +\n" +
            "            \"4. What’s the most meme-worthy or funniest aspect of the subject, if any?\\n\" +\n" +
            "            \"5. Feel free to include any additional information about the subject (or related stories) if you know it.\\n\" +"

    fun getPrompt(content: String?) = promptTemplate +
            "Here’s the text: $content"

    fun getPromptWithTopicName(topic: String?) = 
            "Tell me about $topic\n" +
            promptTemplate
}