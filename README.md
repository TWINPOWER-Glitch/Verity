# Verity (Android)

An Android starter for checking images, video, and audio for generator-related metadata. This is an early vertical slice, not a general AI-media classifier.

## Feasibility

AI-media detection is possible probabilistically, never as a universal proof from content alone. A generator can produce media that detectors cannot distinguish; edits and recompression can erase metadata and forensic traces. This starter recognizes a small set of metadata strings and returns **Unclear** when none match. It has no learned classifier, C2PA signature verification, or audio/video forensic analysis.

## Logic

1. Android document picker returns an image, video, or audio content `Uri`.
2. `MediaMetadataReader` uses `ExifInterface` for images or `MediaMetadataRetriever` for audio/video metadata, on `Dispatchers.IO`.
3. `DetectorCore` checks metadata values for known generator terms.
4. A match yields “Generator trace found”; no match yields “Unclear”. Metadata is editable, so a match is evidence rather than proof.

## Build contract

- Stack: Kotlin, single Activity, Jetpack Compose Material 3, ViewModel + StateFlow.
- Pure signal: `DetectorCore.analyzeMetadata(Map<String, String>): AnalysisResult`.
- Adapter: `MediaMetadataReader.read(Uri)`; descriptors and retrievers are released and I/O runs on cancellable `Dispatchers.IO`.
- Input: image, video, or audio `Uri`; optional embedded metadata; empty metadata stays explicitly unclear.
- Output: `LIKELY_AI` or `UNCLEAR`, evidence text, and nullable confidence (no calibrated probability is claimed).
- Error model: visible UI error with a retry path through image selection.
- Tests: generator metadata match; missing metadata remains unclear.
- Out of scope: learned inference, C2PA verification, content forensics, calibration dataset, and network services.

## Run

Open this directory in Android Studio with Android SDK 36 installed, then run the `app` configuration. The project has not been built in this environment.
