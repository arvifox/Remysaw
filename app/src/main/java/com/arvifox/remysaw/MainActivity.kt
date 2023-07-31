package com.arvifox.remysaw

import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arvifox.remysaw.ui.theme.RemysawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm: MainViewModel by viewModels()
        setContent {
            RemysawTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val state = vm.remyState.collectAsStateWithLifecycle()
                    Column(modifier = Modifier.fillMaxSize()) {
                        TextField(
                            value = state.value.url,
                            onValueChange = vm::onUrlChanged,
                        )
                        Button(onClick = {
                            vm.startService(this@MainActivity)
                        }) {
                            Text(text = "start")
                        }
                    }
                }
            }
        }
    }
}
