import re

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

# Change IdeRepository constructor
content = content.replace(
    'class IdeRepository(private val db: AppDatabase) {',
    '''import android.content.Context
import android.content.SharedPreferences

class IdeRepository(private val db: AppDatabase, private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_ide_settings", Context.MODE_PRIVATE)
'''
)

# Initialize MutableStateFlow with loaded settings
content = content.replace(
    'private val _settings = MutableStateFlow(AppSettings())',
    '''private val _settings = MutableStateFlow(loadSettings())
    
    private fun loadSettings(): AppSettings {
        return AppSettings(
            themeMode = prefs.getString("themeMode", "DARK") ?: "DARK",
            language = prefs.getString("language", "RU") ?: "RU",
            fontSize = prefs.getInt("fontSize", 14),
            animationsEnabled = prefs.getBoolean("animationsEnabled", true),
            biometricsEnabled = prefs.getBoolean("biometricsEnabled", false)
        )
    }
'''
)

# Update updateSettings to save to prefs
content = content.replace(
    '''    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }''',
    '''    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("themeMode", newSettings.themeMode)
            putString("language", newSettings.language)
            putInt("fontSize", newSettings.fontSize)
            putBoolean("animationsEnabled", newSettings.animationsEnabled)
            putBoolean("biometricsEnabled", newSettings.biometricsEnabled)
            apply()
        }
    }'''
)

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)
