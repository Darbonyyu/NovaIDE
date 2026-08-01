with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()
content = content.replace('_chatMessages.value.lastOrNull', 'chatMessages.value.lastOrNull')
with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/ui/screens/ChatScreen.kt', 'r') as f:
    content = f.read()
content = content.replace('onCompareCode = { code -> viewModel.openDiffCompare(code) }', 'onCompareCode = { code -> viewModel.openCodeDiff(code) }')
with open('./app/src/main/java/com/example/ui/screens/ChatScreen.kt', 'w') as f:
    f.write(content)
