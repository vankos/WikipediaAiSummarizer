package com.example.wikipediaaisummarizer.ui

import java.util.Locale

class PromptService {

    val promptTemplate : String  =
            "Act as an charismatic documentary host who is a enthusiastic expert who loves making complex topics simple.\n" +
            "Write in an engaging, conversational, and storytelling style. \n" +
            "If you must choose between an abstract takeaway and a weird specific detail, choose the detail that actually helps to illuminate the “why” and “how” (causes, mechanisms, incentives).\n" +
            "Focus on the \"why\" and \"how\" (the consequences and underlying reasons), but explain them through specific examples rather than general slogans.\n" +
            "Do NOT write a chronological list of events or dates (e.g. \"In 1764..., in 1917..., in 2014...\"). Do not structure the answer as a timeline or list of bullet points. Instead, build 2–3 vivid narrative arcs around people, conflicts, and situations, and weave the facts into those stories. Mention dates only when they are absolutely essential to understand the story.\n" +
            "Prioritize depth over breadth: pick 2–3 key themes or stories about the subject and explore them in detail, rather than listing many separate facts or episodes." +
            "Base everything you say on verifiable facts.\n" +
            "You may include documented interpretations by historians, journalists, economists, and official reports, as long as you present them as interpretations, not absolute truth.\n" +
            "Do NOT invent your own guesses or motives.\n" +
            "DO NOT use complex jargon or technical terms without explaining them simply.\n" +
            "Don’t list dry dates, minor names, or trivia unless they are essential to the story or genuinely surprising.\n" +
            "Don’t write a dry summary; keep it narrative.\n" +
            "Write the response in ${Locale.getDefault().getDisplayLanguage(Locale("en"))}. " +
            "The answer structure should be as follows:\n" +
            "1. State what the subject is known for, as a short narrative, not a list of dates.\n" +
            "2. Describe the most interesting thing about the subject or surprising, local, quirky details or unintended consequences, again as a story rather than a timeline\n" +
            "3. If there’s a notable story, legend, rumor, or specific incident connected to the subject, tell that single story as a self-contained story. Do NOT turn this section into a chronological history or a timeline of the object.\n" +
            "4. What’s the most meme-worthy or funniest aspect of the subject, if any?" +
            "Describe only real memes or jokes that actually exist. Do NOT create new memes yourself.\n" +
            "5. Feel free to include any additional information about the subject (or related stories) if you know it, but keep everything in a narrative form and avoid bullet-pointed timelines.\n"

    fun getPrompt(content: String?): String =
            promptTemplate +
            "Here’s the text: $content"


    fun getPromptWithTopicName(topic: String?) =
            "Tell me about $topic\n" +
            promptTemplate
}
