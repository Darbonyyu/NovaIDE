package com.example.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*

@Composable
fun MainScreen(
    viewModel: IdeViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf<NavTab>(NavTab.Chat) }
    val saveableStateHolder = rememberSaveableStateHolder()

    val currentProject by viewModel.currentProject.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val allProviders by viewModel.allProviders.collectAsState()
    val currentFiles by viewModel.currentFiles.collectAsState()

    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsState()
    val diffToCompare by viewModel.diffToCompare.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                currentProject = currentProject,
                allProjects = allProjects,
                selectedProvider = selectedProvider,
                allProviders = allProviders,
                onSelectProject = { projId -> viewModel.selectProject(projId) },
                onCreateProjectClick = {
                    viewModel.createProject("Новый Проект", "Описание проекта", "Kotlin")
                },
                onSelectProvider = { prov -> viewModel.selectProvider(prov) },
                onOpenCommandPalette = { viewModel.toggleCommandPalette() },
                onToggleLivePreview = {
                    viewModel.toggleLivePreview()
                    currentTab = NavTab.Projects
                },
                onOpenSettings = { currentTab = NavTab.Settings }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentTab.route,
                language = settings.language,
                onTabSelected = { selected -> currentTab = selected }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "TabSwitch") { tab ->
                saveableStateHolder.SaveableStateProvider(tab) {
                    when (tab) {
                    NavTab.Chat -> ChatScreen(
                        viewModel = viewModel,
                        onNavigateToWorkspace = { currentTab = NavTab.Projects }
                    )
                    NavTab.Projects -> ProjectsWorkspaceScreen(viewModel = viewModel)
                    NavTab.History -> HistoryScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { currentTab = NavTab.Chat }
                    )
                    NavTab.Api -> ApiProvidersScreen(viewModel = viewModel)
                    NavTab.Settings -> SettingsScreen(viewModel = viewModel)
                }
                }
            }
        }
    }

    // Command Palette Modal
    if (isCommandPaletteOpen) {
        CommandPaletteModal(
            onDismiss = { viewModel.toggleCommandPalette() },
            projectFiles = currentFiles,
            providers = allProviders,
            onOpenFile = { file ->
                viewModel.openFileTab(file)
                currentTab = NavTab.Projects
            },
            onSelectProvider = { prov -> viewModel.selectProvider(prov) },
            onToggleTerminal = {
                viewModel.toggleTerminal()
                currentTab = NavTab.Projects
            },
            onToggleLivePreview = {
                viewModel.toggleLivePreview()
                currentTab = NavTab.Projects
            },
            onSendMessage = { prompt ->
                viewModel.sendMessage(prompt)
                currentTab = NavTab.Chat
            },
            onClearChat = { viewModel.clearChat() },
            onExportZip = { viewModel.exportProjectZip(context) }
        )
    }

    // Code Diff Comparison Dialog
    diffToCompare?.let { (orig, gen) ->
        CodeDiffDialog(
            originalCode = orig,
            generatedCode = gen,
            onDismiss = { viewModel.closeCodeDiff() },
            onApply = {
                viewModel.replaceActiveTabCode(gen)
                viewModel.closeCodeDiff()
                currentTab = NavTab.Projects
            }
        )
    }
}
