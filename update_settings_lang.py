import re

with open('./app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('"Настройки AI IDE"', 'if (language == "RU") "Настройки AI IDE" else "AI IDE Settings"')
content = content.replace('"Параметры интерфейса, шрифтов, шифрования и бэкапа"', 'if (language == "RU") "Параметры интерфейса, шрифтов, шифрования и бэкапа" else "Interface, fonts, encryption, and backup parameters"')
content = content.replace('"Внешний вид и Тема"', 'if (language == "RU") "Внешний вид и Тема" else "Appearance & Theme"')
content = content.replace('"Тема оформления"', 'if (language == "RU") "Тема оформления" else "Theme"')
content = content.replace('"Тёмная"', 'if (language == "RU") "Тёмная" else "Dark"')
content = content.replace('"Светлая"', 'if (language == "RU") "Светлая" else "Light"')
content = content.replace('"Язык интерфейса"', 'if (language == "RU") "Язык интерфейса" else "Interface Language"')
content = content.replace('"Редактор и Шрифты"', 'if (language == "RU") "Редактор и Шрифты" else "Editor & Fonts"')
content = content.replace('"Размер шрифта в редакторе: ${fontSize.toInt()} sp"', 'if (language == "RU") "Размер шрифта в редакторе: ${fontSize.toInt()} sp" else "Editor font size: ${fontSize.toInt()} sp"')
content = content.replace('"Плавные анимации UI"', 'if (language == "RU") "Плавные анимации UI" else "Smooth UI animations"')
content = content.replace('"Безопасность и Резервное Копирование"', 'if (language == "RU") "Безопасность и Резервное Копирование" else "Security & Backup"')
content = content.replace('"Защита биометрией (Face / Fingerprint)"', 'if (language == "RU") "Защита биометрией (Face / Fingerprint)" else "Biometric protection (Face / Fingerprint)"')
content = content.replace('"Экспорт ZIP"', 'if (language == "RU") "Экспорт ZIP" else "Export ZIP"')
content = content.replace('"Импорт ZIP"', 'if (language == "RU") "Импорт ZIP" else "Import ZIP"')
content = content.replace('"Удалить все локальные данные"', 'if (language == "RU") "Удалить все локальные данные" else "Delete all local data"')
content = content.replace('"Очистить все данные?"', 'if (language == "RU") "Очистить все данные?" else "Clear all data?"')
content = content.replace('"Это действие удалит все проекты, ключи API и историю сообщений."', 'if (language == "RU") "Это действие удалит все проекты, ключи API и историю сообщений." else "This action will delete all projects, API keys and message history."')
content = content.replace('"Удалить"', 'if (language == "RU") "Удалить" else "Delete"')
content = content.replace('"Отмена"', 'if (language == "RU") "Отмена" else "Cancel"')

with open('./app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
