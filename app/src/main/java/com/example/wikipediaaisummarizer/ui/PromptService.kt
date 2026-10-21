package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    private fun promptTemplate(languageDisplayName: String): String =
        """
        Style:
        - Tell me about topic in lively, bold, concrete way, in simple words.
        - Be sceptical
        - Add an explanation of why something happened and how it worked
        Constraints:
        - Do not write a chronological timeline like "In 1764…, in 1917…". 
        - Write exact dates only when the story makes no sense without them. To place events in time, name a period the reader will recognize, such as "during the First World War", "under Ottoman rule", or "during covid".
        - Avoid complex jargon; if you must use a technical term, replace it by more simple one or explain it in simple words.
        - Do not invent your own guesses or motives.
        - Do not include statistics, or any numeric data
        - Do not use direct quotes from the sources, but you can paraphrase them. 
        Write the response in ${languageDisplayName}. The answer structure should be as follows:
        1. Start with what this place/subject is famous or infamous for
        2. Describe the most interesting or surprising thing about subject
        3. Tell one story, legend, rumor or specific incident that connected to the subject.
        4. Tell me about any critics, opposition, or controversies related to the subject (if any). If there are none, just write "None".
        5.  Only if it adds something: facts or connections that show the bigger picture
        """

    fun getPrompt(content: String?, languageDisplayName: String): String =
            promptTemplate(languageDisplayName) +
            "Here’s the text: $content"


    fun getPromptWithTopicName(topic: String?, languageDisplayName: String) =
            "Tell me about $topic\n" +
            promptTemplate(languageDisplayName)
}
