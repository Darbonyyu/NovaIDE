import re

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

# I need to add Context to IdeRepository constructor or initialize settings from SharedPreferences inside IdeViewModel.
# Wait, let's just make IdeViewModel handle SharedPreferences and pass it to IdeRepository or just let IdeViewModel do the SharedPreferences logic.
