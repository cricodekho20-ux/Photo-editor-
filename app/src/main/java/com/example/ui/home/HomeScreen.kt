package com.example.ui.home

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.admin.AdminPinDialog
import com.example.ui.admin.AdminScreen
import com.example.ui.editor.PhotoQualityEnhanceDialog
import com.example.ui.editor.RenameDialog
import com.example.ui.theme.StudioPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenProject: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedEnhancePhotoUri by remember { mutableStateOf<String?>(null) }

    val homePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedEnhancePhotoUri = it.toString()
            }
        }
    )

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    if (uiState.isAdminScreenOpen) {
        AdminScreen(
            adminPosts = uiState.adminPosts,
            onPublishPost = { title, desc, cat, badge, banner ->
                viewModel.publishAdminPost(title, desc, cat, badge, banner)
            },
            onDeletePost = { id -> viewModel.deleteAdminPost(id) },
            onNavigateBack = { viewModel.setAdminScreenOpen(false) }
        )
        return
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                currentTab = uiState.currentTab,
                onTabSelect = { viewModel.setTab(it) },
                onNewProjectClick = { viewModel.setShowNewProjectSheet(true) },
                onAdminClick = { viewModel.setShowAdminPinDialog(true) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowNewProjectSheet(true) },
                containerColor = StudioPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("home_fab_new_project")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Project", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Design", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0A0B10)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                HomeTab.MY_PROJECTS -> {
                    MyProjectsContent(
                        projects = uiState.projects,
                        adminPosts = uiState.adminPosts,
                        onOpen = onOpenProject,
                        onDuplicate = { viewModel.duplicateProject(it) },
                        onRename = { viewModel.setProjectToRename(it) },
                        onDelete = { viewModel.deleteProject(it.id) },
                        onNewClick = { viewModel.setShowNewProjectSheet(true) },
                        onEnhancePhotoClick = {
                            homePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
                HomeTab.TEMPLATES -> {
                    TemplatesContent(
                        templates = uiState.templates,
                        adminPosts = uiState.adminPosts,
                        onSelectTemplate = { template ->
                            viewModel.createFromTemplate(template) { newProj ->
                                onOpenProject(newProj)
                            }
                        },
                        onEnhancePhotoClick = {
                            homePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
                HomeTab.SETTINGS -> {
                    SettingsContent(
                        onOpenAdminPortal = { viewModel.setShowAdminPinDialog(true) }
                    )
                }
            }

            // Quick Photo Quality Enhancer Dialog from Home
            selectedEnhancePhotoUri?.let { uriStr ->
                val tempLayer = CanvasLayer(
                    id = "home_temp_enhance",
                    name = "Photo",
                    type = LayerType.IMAGE,
                    imageUri = uriStr
                )
                PhotoQualityEnhanceDialog(
                    layer = tempLayer,
                    onDismiss = { selectedEnhancePhotoUri = null },
                    onApplyEnhancedLayer = { enhancedUri, w, h ->
                        selectedEnhancePhotoUri = null
                        // Create a project directly with the enhanced photo
                        val ratioLabel = if (w > h) "16:9" else if (w == h) "1:1" else "9:16"
                        val imageLayer = CanvasLayer(
                            id = UUID.randomUUID().toString(),
                            name = "HD Enhanced Photo",
                            type = LayerType.IMAGE,
                            imageUri = enhancedUri,
                            xPercent = 0.5f,
                            yPercent = 0.5f,
                            widthPercent = 0.85f,
                            heightPercent = 0.85f
                        )
                        viewModel.createNewProject(
                            title = "Enhanced Photo Artwork",
                            width = w.coerceIn(720, 2160),
                            height = h.coerceIn(720, 2160),
                            aspectRatioLabel = ratioLabel,
                            layers = listOf(imageLayer)
                        ) { newProj ->
                            onOpenProject(newProj)
                        }
                    }
                )
            }

            // New Project Modal Bottom Sheet
            if (uiState.showNewProjectSheet) {
                NewProjectSheet(
                    onDismiss = { viewModel.setShowNewProjectSheet(false) },
                    onCreateProject = { title, width, height, ratioLabel, bgConfig ->
                        viewModel.createNewProject(title, width, height, ratioLabel, bgConfig) { created ->
                            onOpenProject(created)
                        }
                    }
                )
            }

            // Admin PIN Authentication Dialog (Secret PIN 7255, masked)
            if (uiState.showAdminPinDialog) {
                AdminPinDialog(
                    onDismiss = { viewModel.setShowAdminPinDialog(false) },
                    onPinSuccess = { viewModel.setAdminScreenOpen(true) }
                )
            }

            // Rename Project Dialog
            uiState.projectToRename?.let { project ->
                RenameDialog(
                    currentTitle = project.title,
                    onDismiss = { viewModel.setProjectToRename(null) },
                    onConfirm = { newTitle ->
                        viewModel.renameProject(project, newTitle)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    currentTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit,
    onNewProjectClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    Surface(
        color = Color(0xFF10131D),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            // Brand Name & Admin Access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(StudioPrimary, Color(0xFF7C3AED)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "PixCraft Pro",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Photo & Graphic Studio",
                            color = StudioPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Discreet Admin Shield Entry
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = onNewProjectClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StudioPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tabs: My Projects | Templates | Settings
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = StudioPrimary,
                divider = { Divider(color = Color(0xFF262C3E), thickness = 1.dp) }
            ) {
                Tab(
                    selected = currentTab == HomeTab.MY_PROJECTS,
                    onClick = { onTabSelect(HomeTab.MY_PROJECTS) },
                    text = { Text("My Projects", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = currentTab == HomeTab.TEMPLATES,
                    onClick = { onTabSelect(HomeTab.TEMPLATES) },
                    text = { Text("Templates & Feed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = currentTab == HomeTab.SETTINGS,
                    onClick = { onTabSelect(HomeTab.SETTINGS) },
                    text = { Text("Settings", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    }
}

@Composable
private fun MyProjectsContent(
    projects: List<Project>,
    adminPosts: List<AdminPost>,
    onOpen: (Project) -> Unit,
    onDuplicate: (Project) -> Unit,
    onRename: (Project) -> Unit,
    onDelete: (Project) -> Unit,
    onNewClick: () -> Unit,
    onEnhancePhotoClick: () -> Unit
) {
    val latestBroadcast = adminPosts.firstOrNull { !it.bannerText.isNullOrBlank() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Broadcast Banner from Admin
        if (latestBroadcast != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF151A29),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = latestBroadcast.bannerText ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 1-Tap AI Photo Enhancer Action Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF161A2B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onEnhancePhotoClick() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF7C3AED), StudioPrimary)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "✨ AI Photo Enhancer (HD Quality)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sharpen details, remove blur & upscale 2x / 4x",
                            color = StudioPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onEnhancePhotoClick,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF7C3AED)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Enhance", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (projects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF151924)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = StudioPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No saved projects yet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Create your first design or enhance a photo with AI.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onNewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Design", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onEnhancePhotoClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = StudioPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enhance Photo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onOpen = { onOpen(project) },
                        onDuplicate = { onDuplicate(project) },
                        onRename = { onRename(project) },
                        onDelete = { onDelete(project) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val thumbnailBitmap = remember(project.thumbnailBase64) {
        if (!project.thumbnailBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(project.thumbnailBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF12151F),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF262C3E)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF080A10)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap,
                        contentDescription = project.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${project.layers.size} Layers",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = project.aspectRatioLabel,
                        color = StudioPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${project.width}×${project.height} • ${dateFormat.format(Date(project.updatedAt))}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF151924))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Project", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = Color.White) },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            }
                        )
                        Divider(color = Color(0xFF262C3E))
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFF43F5E)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF43F5E)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatesContent(
    templates: List<ProjectTemplate>,
    adminPosts: List<AdminPost>,
    onSelectTemplate: (ProjectTemplate) -> Unit,
    onEnhancePhotoClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Photo Enhancer Promo
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF161A2B),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEnhancePhotoClick() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF7C3AED), StudioPrimary)),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "✨ AI Photo Enhancer (HD Quality)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sharpen details, remove blur & upscale 2x / 4x",
                                color = StudioPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onEnhancePhotoClick,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Enhance", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Admin Posts Section if available
        if (adminPosts.isNotEmpty()) {
            item {
                Text(
                    text = "📢 Community & Admin Posts",
                    color = StudioPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(adminPosts, key = { "admin_${it.id}" }) { post ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF12151F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = post.badge,
                                    color = StudioPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = post.category,
                                color = Color(0xFF06B6D4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = post.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = post.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "🎨 Curated Starter Templates",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 800.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF12151F),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF262C3E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(template) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(
                                        Brush.linearGradient(template.previewGradientColors.map { Color(it) })
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = template.layers.firstOrNull { it.text.isNotBlank() }?.text?.take(24) ?: template.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = template.aspectRatioLabel,
                                        color = StudioPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = template.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = template.category,
                                    color = StudioPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    onOpenAdminPortal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("PixCraft Pro Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF12151F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Studio Engine", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                SettingItem(title = "Local High-Res Rendering", value = "100% Native")
                SettingItem(title = "Photo Enhancer Engine", value = "Magic HD Active")
                SettingItem(title = "Trending Hindi & English Fonts", value = "Rozha, Kalam, Bebas, Pacifico...")
                SettingItem(title = "Offline Storage Mode", value = "Room SQLite")
            }
        }

        // Admin Portal Entry
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF151924),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAdminPortal() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = StudioPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Admin & Publisher Console", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Manage posts, templates & announcements", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StudioPrimary)
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF12151F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About PixCraft Pro", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Version 2.0.0 Luxury Cyber Edition", color = Color.White, fontSize = 13.sp)
                Text(
                    "Professional mobile photo, poster, and graphic text design studio with multi-layer editing, vector shapes, stickers, trending fonts, and lossless export.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 13.sp)
        Text(value, color = StudioPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
