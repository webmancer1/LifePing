package com.example.lifeping.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifeping.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // onNavigate: (String) -> Unit, // Future navigation callback
    viewModel: HomeViewModel = viewModel(),
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val status by viewModel.status.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    val history by viewModel.checkInHistory.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.75f),
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                drawerContainerColor = Color.White,
            ) {
                DrawerHeader(userName, userEmail)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                DrawerItem(Icons.Default.Home, "Home", true) { scope.launch { drawerState.close() } }
                DrawerItem(Icons.Default.Person, "Profile", false) { 
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                }
                DrawerItem(Icons.Default.Settings, "Settings", false) { /* Navigate */ }
                DrawerItem(Icons.Default.Info, "About", false) { /* Navigate */ }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Placeholder for Logo
                            // Icon(painter = painterResource(id = R.drawable.ic_logo), contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LifePing",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PrimaryBlue
                    )
                )
            },
            containerColor = BackgroundGray
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Card
                StatusCard(status)

                // Next Check-In Card
                NextCheckInCard(countdownText) { viewModel.onCheckInNow() }

                // Stats Grid
                StatsGrid(stats)

                // Recent Check-Ins
                RecentCheckInsSection(history)
            }
        }
    }
}

@Composable
fun DrawerHeader(name: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlue)
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        Column {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.first().toString().uppercase(),
                        color = PrimaryBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = email,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    textColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) PrimaryBlue else textColor
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
            selectedIconColor = PrimaryBlue,
            selectedTextColor = PrimaryBlue,
            unselectedIconColor = textColor,
            unselectedTextColor = textColor
        )
    )
}

@Composable
fun StatusCard(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "You're on track with your check-ins",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                Surface(
                    color = ActiveGreen,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Active",
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NextCheckInCard(timeRemaining: String, onCheckIn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Next Check-In",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = timeRemaining,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "03:30 PM", // Keeping static as per mockup request, but could be dynamic
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Progress bar simulation
            LinearProgressIndicator(
                progress = 0.7f,
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBlue,
                trackColor = BackgroundGray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCheckIn,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Check In Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatsGrid(stats: HomeStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            iconColor = SuccessGreen,
            value = stats.totalCheckIns.toString(),
            label = "Total Check-Ins"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CalendarToday,
            iconColor = PrimaryBlue, // Blue for streak
            value = stats.streakDays.toString(),
            label = "Days Streak"
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    // Third card full width or managed differently? "Stats Cards (Grid Layout)" implies maybe 2 columns.
    // The prompt says "Cards (Grid Layout) ... Card 3 - Missed Check-Ins".
    // I'll put the third card below or in a flow. Let's put it solitary for now to match structure.
    StatCard(
        modifier = Modifier.fillMaxWidth(),
        icon = Icons.Default.Warning,
        iconColor = WarningOrange,
        value = stats.missedCheckIns.toString(),
        label = "Missed Check-Ins"
    )
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RecentCheckInsSection(history: List<CheckInItem>) {
    Text(
        text = "Recent Check-Ins",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    Text(
        text = "Your check-in history",
        fontSize = 14.sp,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        history.take(4).forEach { item ->
            RecentCheckInItem(item)
        }
    }
}

@Composable
fun RecentCheckInItem(item: CheckInItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier
                        .size(24.dp)
                        .background(ActiveGreen, CircleShape)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.time,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Surface(
                color = ActiveGreen,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.status,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
