package com.example.verity

import java.util.Locale

enum class Verdict { LIKELY_AI, UNCLEAR }

data class AnalysisResult(
    val verdict: Verdict,
    val evidence: List<String>,
    val confidence: Float?,
)

object DetectorCore {
    private val generatorTerms = listOf(
        "stable diffusion", "midjourney", "dall-e", "dall·e", "comfyui",
        "adobe firefly", "automatic1111", "trainedalgorithmicmedia",
    )

    fun analyzeMetadata(values: Map<String, String>): AnalysisResult {
        val matches = values.values
            .asSequence()
            .map { it.lowercase(Locale.ROOT) }
            .flatMap { value -> generatorTerms.asSequence().filter(value::contains) }
            .distinct()
            .toList()

        return if (matches.isNotEmpty()) {
            AnalysisResult(
                verdict = Verdict.LIKELY_AI,
                evidence = listOf("Generator-related metadata: ${matches.joinToString()}. Metadata can be edited."),
                confidence = null,
            )
        } else {
            AnalysisResult(
                verdict = Verdict.UNCLEAR,
                evidence = listOf("No recognized generator trace was found. This does not establish that the image is camera-made."),
                confidence = null,
            )
        }
    }
}
