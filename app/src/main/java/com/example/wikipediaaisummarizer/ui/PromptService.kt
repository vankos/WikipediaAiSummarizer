package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    private fun promptTemplate(languageDisplayName: String): String =
        """Act as an charismatic documentary host who is a enthusiastic expert who loves making complex topics simple.
        Style:
        - Whenever you state a fact, explain **why** and **how** it worked in practice if applies. Show mechanisms, incentives and consequences through specific examples, not slogans, You may include documented interpretations by existing sources.
        Constraints:
        - Do NOT write a chronological timeline like "In 1764…, in 1917…". 
        - Do NOT write dates only when absolutely essential to understand the story. You can specify decade or century for understanding approximate period.  
        - Avoid complex jargon; if you must use a technical term, replace it by more simple one or explain it in simple words.
        - Do NOT invent your own guesses or motives.
        - Do NOT write stats or much numbers 
        Write the response in ${languageDisplayName}. The answer structure should be as follows:
        1. Start with what this place/subject is famous or infamous for
        2. Describe the most interesting or surprising thing about subject
        3. Tell one story, legend, rumor or specific incident that connected to the subject.
        4. What's the most meme-worthy or funniest aspect of the subject, if any? Describe only real memes or jokes that actually exist. Do NOT create new memes yourself.
        5. Tell me about any critics, opposition, or controversies related to the subject (if any). If there are none, just write "None".
        6. Add any extra facts, stories or connections that help understand the bigger picture
        """

    fun getPrompt(content: String?, languageDisplayName: String): String =
            promptTemplate(languageDisplayName) +
            "Here’s the text: $content"


    fun getPromptWithTopicName(topic: String?, languageDisplayName: String) =
            "Tell me about $topic\n" +
            promptTemplate(languageDisplayName)
}
