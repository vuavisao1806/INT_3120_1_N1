package com.example.locationpins.ui.screen.badges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationpins.data.mapper.toBadgeProgressList
import com.example.locationpins.data.mapper.toBadges
import com.example.locationpins.data.model.Badge
import com.example.locationpins.data.model.BadgeProgress
import com.example.locationpins.data.repository.BadgeRepository
import com.example.locationpins.ui.component.BadgeDetailCard
import com.example.locationpins.ui.component.BadgeProgressCard
import com.example.locationpins.ui.screen.login.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyBadgesUiState(
    val isLoading: Boolean = true,
    val earnedBadges: List<Badge> = emptyList(),
    val progressBadges: List<BadgeProgress> = emptyList(),
    val error: String? = null
)

class MyBadgesViewModel(
    private val badgeRepository: BadgeRepository = BadgeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBadgesUiState())
    val uiState: StateFlow<MyBadgesUiState> = _uiState.asStateFlow()

    fun loadBadges() {
        val userId = CurrentUser.currentUser?.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val earned = badgeRepository.getEarnedBadges(userId, limit = 100).toBadges()
                val progress = badgeRepository.getBadgeProgress(userId).toBadgeProgressList()

                // Sắp xếp progress theo % hoàn thành (cao → thấp)
                val sortedProgress = progress.sortedByDescending { it.progressPercentage }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        earnedBadges = earned,
                        progressBadges = sortedProgress
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải huy hiệu: ${e.message}"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBadgesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyBadgesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadBadges()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Huy hiệu của tôi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Đã đạt được (${uiState.earnedBadges.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Tiến trình (${uiState.progressBadges.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Đã xảy ra lỗi",
                                color = Color.Red
                            )
                            Button(onClick = { viewModel.loadBadges() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }

                else -> {
                    when (selectedTab) {
                        0 -> EarnedBadgesTab(badges = uiState.earnedBadges)
                        1 -> ProgressBadgesTab(progress = uiState.progressBadges)
                    }
                }
            }
        }
    }
}

@Composable
private fun EarnedBadgesTab(badges: List<Badge>) {
    if (badges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có huy hiệu nào",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(badges) { badge ->
                BadgeDetailCard(badge = badge)
            }
        }
    }
}

@Composable
private fun ProgressBadgesTab(progress: List<BadgeProgress>) {
    if (progress.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Không có dữ liệu",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(progress) { item ->
                BadgeProgressCard(progress = item)
            }
        }
    }
}