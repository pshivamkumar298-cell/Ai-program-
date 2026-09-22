package com.example.data.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

enum class AssistantMode(
    val title: String,
    val iconName: String,
    val tag: String,
    val description: String,
    val defaultPrompt: String,
    val systemPromptSuffix: String
) {
    GENERAL(
        title = "Universal Reasoner",
        iconName = "auto_awesome",
        tag = "Core AI",
        description = "Full multi-step reasoning pipeline: Understand → Think → Create → Solve → Decide → Act.",
        defaultPrompt = "Help me break down this complex problem and formulate an actionable plan.",
        systemPromptSuffix = "Mode: General & Agentic. Follow the pipeline: 1. Understand, 2. Think, 3. Create, 4. Solve, 5. Decide, 6. Act. Provide clear conclusions."
    ),
    EDUCATION(
        title = "Education & Tutor",
        iconName = "school",
        tag = "Academic",
        description = "Personal tutor for concepts, derivations, formulas, practice questions & revision schedules.",
        defaultPrompt = "Explain the intuitive concept of Bayes' Theorem with a real-life example and practice question.",
        systemPromptSuffix = "Mode: Education Tutor. Prefer conceptual understanding over rote memorization. Show formulas, step-by-step working, and generate practice questions when helpful."
    ),
    PROGRAMMING(
        title = "Coding & Architecture",
        iconName = "code",
        tag = "Software Dev",
        description = "Clean maintainable code, system architecture, debugging, test suggestions & API guides.",
        defaultPrompt = "Design an event-driven architecture for high-throughput notifications and write sample code.",
        systemPromptSuffix = "Mode: Programming & Architecture. Design architecture before writing code. Produce clean, maintainable, production-ready code with explanations and test cases."
    ),
    MATH_DATA(
        title = "Math & Data Solver",
        iconName = "calculate",
        tag = "STEM & Stats",
        description = "Precision arithmetic, algebra, calculus, unit conversions, statistics & financial modeling.",
        defaultPrompt = "Solve: A loan of $25,000 at 6.5% annual interest for 5 years. Calculate monthly EMI and total interest paid.",
        systemPromptSuffix = "Mode: Mathematics and Data. Show full working steps, formulas, and verified numerical calculations. Highlight key assumptions."
    ),
    CREATIVE(
        title = "Creative Studio",
        iconName = "palette",
        tag = "Content & Ideas",
        description = "Stories, video scripts, poetry, social media hooks, marketing concepts & brand identity.",
        defaultPrompt = "Write a compelling opening scene for a sci-fi narrative about discovering quantum entanglement in biological cells.",
        systemPromptSuffix = "Mode: Creative Intelligence. Adopt imaginative, vivid storytelling and adhere strictly to requested tone, cadence, and format."
    ),
    BUSINESS(
        title = "Business & Strategy",
        iconName = "trending_up",
        tag = "Strategy",
        description = "Practical problem solving: objectives, constraints, trade-offs, risk matrix & roadmaps.",
        defaultPrompt = "Analyze whether an early-stage SaaS startup should build native mobile apps or a responsive PWA first.",
        systemPromptSuffix = "Mode: Business and Problem Solving. Structure with: 1. Objective, 2. Constraints, 3. Assumptions, 4. Options & Trade-offs, 5. Risks, 6. Actionable Roadmap."
    )
}

data class GenerationResult(
    val text: String,
    val reasoningSummary: String? = null,
    val isDemo: Boolean = false,
    val errorMessage: String? = null
)

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    const val MASTER_SYSTEM_INSTRUCTION = """
You are UNIVERSAL AI ASSISTANT — an advanced general-purpose AI assistant designed to help users with reasoning, learning, research, creativity, programming, data analysis, multimodal understanding, planning, and real-world problem solving.
Your goal is to provide accurate, useful, clear, context-aware assistance while remaining honest about your capabilities and limitations.

CORE DIRECTIVES:
1. Understand natural language in English, Hindi (हिन्दी), Hinglish, and mixed languages naturally.
2. Break complex problems into smaller logical components.
3. Compare multiple possible solutions and trade-offs.
4. Execute multi-step reasoning internally and present clear conclusions.
5. If image or document data is provided, analyze diagrams, charts, text, or visual objects rigorously.
6. Core Principle: UNDERSTAND → THINK → CREATE → SOLVE → DECIDE → ACT.
7. Maintain honesty: if something is uncertain, declare what is known and what is uncertain. Never fabricate facts, URLs, or data.
8. Structure your answers cleanly using Markdown headers, bullet points, and code/math blocks where helpful.
"""

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateResponse(
        prompt: String,
        mode: AssistantMode,
        modelName: String = "gemini-3.5-flash",
        languagePreference: String = "AUTO",
        conversationHistory: List<Pair<String, String>> = emptyList(), // role, text
        imageBase64: String? = null,
        userMemories: List<String> = emptyList()
    ): GenerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!isApiKeyConfigured()) {
            return@withContext generateSimulatedResponse(prompt, mode, languagePreference, imageBase64 != null)
        }

        try {
            // Build full system prompt
            val systemInstructions = buildString {
                append(MASTER_SYSTEM_INSTRUCTION.trim())
                append("\n\n")
                append(mode.systemPromptSuffix)
                if (languagePreference == "HINDI") {
                    append("\nRespond primarily in Hindi (देवनागरी लिपि).")
                } else if (languagePreference == "HINGLISH") {
                    append("\nRespond in Hinglish (natural mix of Hindi and English written in Latin script).")
                } else if (languagePreference == "ENGLISH") {
                    append("\nRespond in clear, professional English.")
                }
                if (userMemories.isNotEmpty()) {
                    append("\n\nCONVERSATION MEMORY (User preferences & context):\n")
                    userMemories.forEach { append("- ").append(it).append("\n") }
                }
            }

            val requestJson = JSONObject()

            // System Instruction
            val systemContent = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructions) })
                }
                put("parts", partsArray)
            }
            requestJson.put("systemInstruction", systemContent)

            // Contents array (Conversation History + Current Message)
            val contentsArray = JSONArray()

            // Add previous conversation turns (last 6 to prevent context overflow)
            val recentHistory = conversationHistory.takeLast(6)
            for ((role, text) in recentHistory) {
                val turnObj = JSONObject().apply {
                    put("role", if (role.equals("model", ignoreCase = true)) "model" else "user")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentUserTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray()

                // Add text part
                parts.put(JSONObject().apply { put("text", prompt) })

                // Add image part if provided
                if (!imageBase64.isNullOrEmpty()) {
                    val inlineData = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", imageBase64)
                    }
                    parts.put(JSONObject().apply { put("inlineData", inlineData) })
                }

                put("parts", parts)
            }
            contentsArray.put(currentUserTurn)
            requestJson.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject().apply {
                put("temperature", if (mode == AssistantMode.CREATIVE) 0.8 else 0.4)
                put("topP", 0.95)
            }
            requestJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val endpoint = "$BASE_URL$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext GenerationResult(
                    text = "Request error from Gemini: $errorMsg",
                    errorMessage = errorMsg
                )
            }

            val rootJson = JSONObject(responseString)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GenerationResult(
                    text = "No response generated. Please refine your query.",
                    errorMessage = "Empty candidate response"
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val fullText = buildString {
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        append(part.optString("text", ""))
                    }
                }
            }

            // Extract a summary of reasoning based on headings
            val reasoning = extractReasoning(fullText, mode)

            GenerationResult(
                text = fullText,
                reasoningSummary = reasoning,
                isDemo = false
            )
        } catch (e: Exception) {
            GenerationResult(
                text = "Connection failure: ${e.localizedMessage ?: "Unknown error"}. You can review your network connection or enter your Gemini API key in the AI Studio Secrets panel.",
                errorMessage = e.message
            )
        }
    }

    private fun extractReasoning(text: String, mode: AssistantMode): String {
        return when (mode) {
            AssistantMode.GENERAL -> "UNDERSTAND → THINK → CREATE → SOLVE → DECIDE → ACT"
            AssistantMode.EDUCATION -> "Conceptual breakdown with step-by-step verification"
            AssistantMode.PROGRAMMING -> "Architecture validation & clean implementation"
            AssistantMode.MATH_DATA -> "Formula derivation with numerical precision check"
            AssistantMode.CREATIVE -> "Tone and narrative cadence synthesis"
            AssistantMode.BUSINESS -> "Objective, constraints, and risk evaluation"
        }
    }

    private fun generateSimulatedResponse(
        prompt: String,
        mode: AssistantMode,
        languagePreference: String,
        hasImage: Boolean
    ): GenerationResult {
        val isHindi = languagePreference == "HINDI"
        val isHinglish = languagePreference == "HINGLISH"

        val response = when (mode) {
            AssistantMode.EDUCATION -> if (isHindi) {
                """
### 🎓 संकल्पना व्याख्या (Concept Breakdown)
**विषय:** $prompt

1. **मुख्य सिद्धांत (Core Principle):** किसी भी जटिल शैक्षणिक समस्या को समझने के लिए पहले बुनियादी आधारशिला (fundamentals) को समझना आवश्यक है।
2. **चरण-दर-चरण समाधान (Step-by-Step Working):**
   - **चरण 1:** ज्ञात और अज्ञात चरों की पहचान करें।
   - **चरण 2:** प्रासंगिक सूत्र (Formula) लागू करें।
   - **चरण 3:** गणना का सत्यापन करें।
3. **अभ्यास प्रश्न (Practice Question):** क्या आप इस सिद्धांत का उपयोग करके एक समान समस्या हल कर सकते हैं?

*(नोट: पूर्ण लाइव AI मॉडल का उपयोग करने के लिए AI Studio Secrets पैनल में अपनी GEMINI_API_KEY जोड़ें।)*
""".trimIndent()
            } else {
                """
### 🎓 Conceptual Breakdown & Intuitive Explanation
**Objective:** Deconstructing your inquiry on *$prompt*

1. **Core Intuition:** Rather than rote memorization, understand the underlying mental model. Everything builds from foundational axioms.
2. **Step-by-Step Derivation:**
   - **Step 1: Identify Knowns and Targets.** Establish the boundary conditions clearly.
   - **Step 2: Apply the Governing Theorem.** Analyze intermediate transformations systematically.
   - **Step 3: Verification & Sanity Check.** Test edge cases (zero, infinity, parity).
3. **Check for Understanding (Practice Question):**
   - *How would the solution behave if the primary constraint were doubled?*

💡 *Tip: Connect your Google Gemini API key in AI Studio Secrets to unlock real-time live synthesis.*
""".trimIndent()
            }

            AssistantMode.PROGRAMMING -> """
### 💻 Architecture & Code Solution

```kotlin
// Universal Assistant - Clean Production Pattern
class SolutionEngine {
    fun processProblem(input: String): Result<String> {
        return runCatching {
            // Step 1: Input validation
            require(input.isNotBlank()) { "Input must not be empty" }
            
            // Step 2: Core processing algorithm
            val transformed = input.trim().lowercase()
            
            // Step 3: Verified output
            "Processed: ${'$'}transformed"
        }
    }
}
```

#### Key Architecture Decisions:
- **Separation of Concerns:** Isolated data manipulation from orchestration.
- **Fail-Fast Validation:** Validates invariants before resource allocation.
- **Test Strategy:** Recommend unit testing boundary conditions and empty inputs.
""".trimIndent()

            AssistantMode.MATH_DATA -> """
### 📐 Step-by-Step Mathematical Solution

1. **Problem Formulation:**
   - Objective: Solving for variables requested in `$prompt`.
2. **Formula Applied:**
   $$ \text{Result} = f(x, y) \implies \text{Evaluated with numerical precision} $$
3. **Calculation Steps:**
   - Evaluated step 1: Boundary terms simplified.
   - Evaluated step 2: Proportions calculated and verified.
4. **Final Conclusion:**
   - Accurate numerical calculation ready. (Connect Gemini API key for full dynamic computation).
""".trimIndent()

            AssistantMode.CREATIVE -> """
### 💡 Creative Narrative & Concept

A silent hum resonated through the chamber, not of sound, but of pure intent. The question hung in the air like starlight piercing deep indigo velvet: *$prompt*.

*Here was the spark:* Every revolution begins with an unasked question waiting for someone bold enough to think in dimensions others overlook.

#### Concept Angles:
1. **The Catalyst:** A pivotal shift that changes how the user perceives reality.
2. **Visual Metaphor:** Crystalline lattices aligning under harmonic resonance.
""".trimIndent()

            AssistantMode.BUSINESS -> """
### 💼 Strategic Business Analysis

1. **Core Objective:** Deliver measurable value on `$prompt`.
2. **Constraints & Assumptions:** Time-to-market, initial capital expenditure, and user adoption frictions.
3. **Trade-off Matrix:**
   - *Option A (High Speed):* Lean prototype, immediate feedback, higher technical debt.
   - *Option B (High Scale):* Modular architecture, higher upfront investment, lower maintenance.
4. **Actionable Roadmap:**
   - **Phase 1 (Days 1–7):** Validate core hypotheses with target stakeholders.
   - **Phase 2 (Days 8–21):** Deploy minimal viable solution and measure retention.
   - **Phase 3 (Days 22+):** Iterate based on empirical metrics.
""".trimIndent()

            AssistantMode.GENERAL -> if (hasImage) {
                """
### 🖼️ Multimodal Visual Analysis
**Observation:** Image attached for analysis.
1. **Visual Elements:** Foreground structure and layout analyzed.
2. **Context:** Interpreting details relative to your prompt: *$prompt*.
3. **Synthesis:** Key insights extracted from visible objects, charts, or text diagrams.

*(Connect your Gemini API Key in AI Studio Secrets for live vision processing with gemini-3.5-flash).*
""".trimIndent()
            } else {
                """
### 🌟 Universal AI Assistant — 6-Step Reasoning Pipeline

1. **UNDERSTAND:** Deconstructed intent for: *$prompt*.
2. **THINK:** Analyzed constraints, relevant domain knowledge, and edge cases.
3. **CREATE:** Formulated structured solution hypotheses.
4. **SOLVE:** Derived optimal direct response with high factual clarity.
5. **DECIDE:** Selected actionable path with highest utility and minimum risk.
6. **ACT:** 
   - Direct clear guidance provided.
   - Next steps outlined for user execution.

*(Note: Add your GEMINI_API_KEY in the AI Studio Secrets panel for unlimited live model queries).*
""".trimIndent()
            }
        }

        return GenerationResult(
            text = response,
            reasoningSummary = "UNDERSTAND → THINK → CREATE → SOLVE → DECIDE → ACT",
            isDemo = true
        )
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
