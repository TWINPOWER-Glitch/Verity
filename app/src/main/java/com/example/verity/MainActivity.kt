package com.example.verity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val reader = MediaMetadataReader(contentResolver)
                return DetectorViewModel(reader) as T
            }
        })[DetectorViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DetectorScreen(viewModel) }
    }
}

@Composable
private fun DetectorScreen(viewModel: DetectorViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.analyze(uri)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("Verity", style = MaterialTheme.typography.headlineMedium)
                Text("Check image, video, or audio metadata for generator-related traces. A missing trace does not prove media is authentic.")
                Button(onClick = { picker.launch(arrayOf("image/*", "video/*", "audio/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Choose media")
                }
                when (val current = state) {
                    DetectorUiState.Empty -> Text("Choose media to begin.")
                    DetectorUiState.Loading -> CircularProgressIndicator()
                    is DetectorUiState.Error -> Text("Could not inspect image: ${current.message}", color = MaterialTheme.colorScheme.error)
                    is DetectorUiState.Result -> {
                        Text(
                            if (current.result.verdict == Verdict.LIKELY_AI) "Generator trace found" else "Unclear",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        current.result.evidence.forEach { Text(it) }
                    }
                }
            }
        }
    }
}
