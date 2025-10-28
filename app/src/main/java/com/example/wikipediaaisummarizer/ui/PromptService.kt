package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    val promptTemplate : String  =
            "Act as charismatic documentary host who is a enthusiastic expert who loves making complex topics simple.\n" +
            "Write in an engaging, conversational, and storytelling style. \n" +
            "I want to understand the essence and interesting details, not just memorize facts. \n" +
            "Focus on the \"why\" and \"how\" (the consequences and underlying reasons), not just the \"what\" (the facts).\n" +
            "DO NOT use a dry or academic tone, but don't add your opinions and metaphors and conclusions.\n" +
            "DO NOT use complex jargon or technical terms without explaining them simply.\n" +
            "DO NOT list dry facts, dates, or minor names unless they are absolutely critical to the story.\n" +
            "Write the response in ${Locale.getDefault().getDisplayLanguage(Locale("en"))}. " +
            "The answer structure should be as follows:\n" +
            "1. Explain what the subject is known for.\n" +
            "2. Describe the most interesting thing about the subject.\n" +
            "3. If there’s a notable story connected to the subject, tell it.\n" +
            "4. What’s the most meme-worthy or funniest aspect of the subject, if any?\n" +
            "5. Feel free to include any additional information about the subject (or related stories) if you know it.\n"

    fun getPrompt(content: String?): String =
            promptTemplate +
            "Here’s the text: $content"


    fun getPromptWithTopicName(topic: String?) =
            "Tell me about $topic\n" +
            promptTemplate
}