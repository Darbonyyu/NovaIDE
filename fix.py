with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if '❌ Ошибка' in line:
        pass

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    for line in lines:
        f.write(line)
