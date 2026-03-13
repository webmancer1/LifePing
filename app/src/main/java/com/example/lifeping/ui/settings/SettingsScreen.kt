package com.example.lifeping.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lifeping.data.model.Contact
import com.example.lifeping.ui.components.LifePingAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val checkInInterval by viewModel.checkInInterval.collectAsState()
    val gracePeriod by viewModel.gracePeriod.collectAsState()
    val notifyUpcoming by viewModel.notifyUpcoming.collectAsState()
    val autoAlert by viewModel.autoAlert.collectAsState()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.setNotifyUpcoming(true)
            }
        }
    )

    var showAddContactDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LifePingAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddContactDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.background else BgSettings
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trusted Contacts Section
            item {
                SettingsSectionCard(title = "Trusted Contacts", icon = Icons.Default.Add) { // Using Add icon as placeholder for section icon if generic found, or specific
                    Text(
                        text = "People to notify if you miss a check-in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (contacts.isEmpty()) {
                        Text(
                            text = "No trusted contacts added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        contacts.forEach { contact ->
                            ContactItem(
                                contact = contact,
                                onEdit = { contactToEdit = contact },
                                onDelete = { viewModel.deleteContact(contact) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Check-In Schedule Section
            item {
                SettingsSectionCard(title = "Check-In Schedule", icon = Icons.Default.Schedule) {
                    DropdownSetting(
                        title = "Check-In Interval",
                        currentValue = formatDuration(checkInInterval),
                        options = listOf(
                            "2 hours" to 2 * 60 * 60 * 1000L,
                            "4 hours" to 4 * 60 * 60 * 1000L,
                            "8 hours" to 8 * 60 * 60 * 1000L,
                            "12 hours" to 12 * 60 * 60 * 1000L,
                            "24 hours" to 24 * 60 * 60 * 1000L
                        ),
                        onOptionSelected = { viewModel.setCheckInInterval(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DropdownSetting(
                        title = "Grace Period",
                        currentValue = formatDuration(gracePeriod),
                        options = listOf(
                            "15 minutes" to 15 * 60 * 1000L,
                            "30 minutes" to 30 * 60 * 1000L,
                            "1 hour" to 60 * 60 * 1000L
                        ),
                        onOptionSelected = { viewModel.setGracePeriod(it) }
                    )
                    Text(
                        text = "Contacts will be alerted ${formatDuration(gracePeriod)} after a missed check-in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Notification Settings Section
            item {
                SettingsSectionCard(title = "Notification Settings", icon = Icons.Default.Notifications) {
                    SwitchSetting(
                        title = "Push Notifications",
                        description = "Receive notifications for upcoming check-ins",
                        checked = notifyUpcoming,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotifyUpcoming(true)
                                }
                            } else {
                                viewModel.setNotifyUpcoming(false)
                            }
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    SwitchSetting(
                        title = "Auto-Alert System",
                        description = "Automatically notify contacts if you miss a check-in",
                        checked = autoAlert,
                        onCheckedChange = { viewModel.setAutoAlert(it) }
                    )
                }
            }

            // Data Management Section
            item {
                SettingsSectionCard(title = "Data Management", icon = Icons.Default.Warning) {
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Home Page Data")
                    }
                    Text(
                        text = "This will clear your check-in history, total count, and streak. Your next check-in time and schedule will NOT be affected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    if (showAddContactDialog) {
        ContactDialog(
            onDismiss = { showAddContactDialog = false },
            onSave = { contact ->
                viewModel.addContact(contact)
                showAddContactDialog = false
            }
        )
    }

    if (contactToEdit != null) {
        ContactDialog(
            contact = contactToEdit,
            onDismiss = { contactToEdit = null },
            onSave = { contact ->
                viewModel.updateContact(contact)
                contactToEdit = null
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Data?") },
            text = { Text("Are you sure you want to reset your home page data? This action cannot be undone, but your next check-in countdown will remain undisturbed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetHistoryData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable

fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${contact.relationship} • ${contact.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val channels = mutableListOf<String>()
                if (contact.notifyViaSms) channels.add("SMS")
                if (contact.notifyViaEmail) channels.add("Email")
                if (contact.notifyViaWhatsapp) channels.add("WhatsApp")
                
                if (channels.isNotEmpty()) {
                    Text(
                        text = "Alert via: ${channels.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DropdownSetting(
    title: String,
    currentValue: String,
    options: List<Pair<String, Long>>,
    onOptionSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = currentValue)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onOptionSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDialog(
    contact: Contact? = null,
    onDismiss: () -> Unit,
    onSave: (Contact) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var relationship by remember { mutableStateOf(contact?.relationship ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var phone by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var notifyViaSms by remember { mutableStateOf(contact?.notifyViaSms ?: true) }
    var notifyViaEmail by remember { mutableStateOf(contact?.notifyViaEmail ?: false) }
    var notifyViaWhatsapp by remember { mutableStateOf(contact?.notifyViaWhatsapp ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (contact == null) "Add Contact" else "Edit Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = relationship, onValueChange = { relationship = it }, label = { Text("Relationship (e.g. Sister, Friend)") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })

                Text(text = "Notification Methods", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyViaSms, onCheckedChange = { notifyViaSms = it })
                    Text("SMS", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = false, onCheckedChange = null, enabled = false)
                    Text("Email (Coming soon)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = false, onCheckedChange = null, enabled = false)
                    Text("WhatsApp (Coming soon)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Contact(
                            id = contact?.id ?: 0,
                            name = name,
                            relationship = relationship,
                            email = email,
                            phoneNumber = phone,
                            notifyViaSms = notifyViaSms,
                            notifyViaEmail = false, // Forced false as it's coming soon
                            notifyViaWhatsapp = false // Forced false as it's coming soon
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
    return if (hours > 0) {
        "$hours hours"
    } else {
        "$minutes minutes"
    }
}
