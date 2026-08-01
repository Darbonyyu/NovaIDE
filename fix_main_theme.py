import re
with open('./app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: com.example.ui.IdeViewModel = viewModel()
            val settings = viewModel.settings.collectAsState().value
            
            val isDark = when (settings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            
            AiIdeTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}"""

content = re.sub(r'class MainActivity : ComponentActivity\(\) \{[\s\S]*?\}\n\}', replacement, content)

if 'import androidx.lifecycle.viewmodel.compose.viewModel' not in content:
    content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.foundation.isSystemInDarkTheme')

with open('./app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
