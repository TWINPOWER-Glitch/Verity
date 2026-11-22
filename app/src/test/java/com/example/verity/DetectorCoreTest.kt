package com.example.verity

import org.junit.Assert.assertEquals
import org.junit.Test

class DetectorCoreTest {
    @Test fun generatorTraceReturnsLikelyAiEvidence() {
        val result = DetectorCore.analyzeMetadata(mapOf("Software" to "Stable Diffusion XL"))
        assertEquals(Verdict.LIKELY_AI, result.verdict)
    }

    @Test fun absentMetadataIsUnclearRatherThanLikelyReal() {
        val result = DetectorCore.analyzeMetadata(emptyMap())
        assertEquals(Verdict.UNCLEAR, result.verdict)
        assertEquals(null, result.confidence)
    }
}
