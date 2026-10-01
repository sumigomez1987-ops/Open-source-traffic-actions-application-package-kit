package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.DepositRequest
import com.example.data.model.RemoteJob
import com.example.data.model.User
import com.example.data.model.WithdrawalRequest
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isAdmin = viewModel.isAdmin

    if (!isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Access Restricted",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.get("admin_access_denied", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = remember(language) {
        listOf(
            Strings.get("pending_deposits_tab", language),
            Strings.get("pending_withdrawals_tab", language),
            Strings.get("users_tab", language),
            Strings.get("jobs_tab", language),
            Strings.get("settings_tab", language)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.get("admin_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Logged as Admin: ${settings.adminEmail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "MASTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs Row (Scrollable)
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTab) {
            0 -> AdminDepositsTab(viewModel = viewModel, language = language)
            1 -> AdminWithdrawalsTab(viewModel = viewModel, language = language)
            2 -> AdminUsersTab(viewModel = viewModel, language = language)
            3 -> AdminJobsTab(viewModel = viewModel, language = language)
            4 -> AdminSettingsTab(viewModel = viewModel, settings = settings, language = language)
        }
    }
}

// Tab 0: Pending Deposits
@Composable
private fun AdminDepositsTab(viewModel: AppViewModel, language: AppLanguage) {
    val deposits by viewModel.allDeposits.collectAsState()

    if (deposits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No deposits recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(deposits, key = { it.id }) { dep ->
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${dep.method.uppercase()} • ৳${dep.amountBdt} (${dep.coins} Coins)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(status = dep.status, language = language)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "User: ${dep.userEmail}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Sender: ${dep.senderNumber}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "TrxID: ${dep.trxId}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (dep.status == "pending") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.approveDeposit(dep.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.get("approve", language), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.rejectDeposit(dep.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.get("reject", language), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tab 1: Pending Withdrawals
@Composable
private fun AdminWithdrawalsTab(viewModel: AppViewModel, language: AppLanguage) {
    val withdrawals by viewModel.allWithdrawals.collectAsState()

    if (withdrawals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No withdrawals recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(withdrawals, key = { it.id }) { with ->
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${with.method.uppercase()} • ৳${with.amountBdt} (${with.coins} Coins)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(status = with.status, language = language)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "User: ${with.userEmail}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Receiver Account: ${with.receiverNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (with.status == "pending") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.approveWithdrawal(with.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.get("approve", language), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.rejectWithdrawal(with.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.get("reject", language), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tab 2: Users Management
@Composable
private fun AdminUsersTab(viewModel: AppViewModel, language: AppLanguage) {
    val users by viewModel.allUsers.collectAsState()
    var editingUser by remember { mutableStateOf<User?>(null) }
    var coinInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(users, key = { it.uid }) { user ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = user.displayName.ifEmpty { "User" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (user.isBlocked) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "BLOCKED",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Balance: ${user.coins} 🪙",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldCoin
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Modify Balance Button
                            OutlinedButton(
                                onClick = {
                                    editingUser = user
                                    coinInput = user.coins.toString()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Edit Coins", fontSize = 11.sp)
                            }

                            // Block / Unblock Button
                            Button(
                                onClick = { viewModel.toggleUserBlock(user.uid) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isBlocked) EmeraldSuccess else MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (user.isBlocked) Strings.get("unblock_user", language) else Strings.get("block_user", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Coin Balance Dialog
    if (editingUser != null) {
        val targetUser = editingUser!!
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text(Strings.get("modify_balance", language)) },
            text = {
                Column {
                    Text("Modify coin balance for ${targetUser.email}")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = coinInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) coinInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Coins") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCoins = coinInput.toLongOrNull() ?: targetUser.coins
                        viewModel.modifyUserBalance(targetUser.uid, newCoins)
                        editingUser = null
                    }
                ) {
                    Text(Strings.get("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }
}

// Tab 3: Remote Jobs Management
@Composable
private fun AdminJobsTab(viewModel: AppViewModel, language: AppLanguage) {
    val jobs by viewModel.remoteJobs.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(jobs, key = { it.id }) { job ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = job.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${if (job.category == "youtube") "YOUTUBE" else "WEBSITE / BLOGGER"} • ${job.completedCount}/${job.targetVisitors} visits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = job.url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteRemoteJob(job.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// Tab 4: App & Firebase Settings
@Composable
private fun AdminSettingsTab(
    viewModel: AppViewModel,
    settings: AppSettings,
    language: AppLanguage
) {
    val context = LocalContext.current
    var adminEmail by remember { mutableStateOf(settings.adminEmail) }
    var developerName by remember { mutableStateOf(settings.developerName) }
    var facebookUrl by remember { mutableStateOf(settings.facebookUrl) }
    var whatsappNumber by remember { mutableStateOf(settings.whatsappNumber) }
    var sendMoneyNumber by remember { mutableStateOf(settings.sendMoneyNumber) }
    var aboutNoticeBn by remember {
        mutableStateOf(
            if (settings.aboutNoticeBn.isNotBlank()) settings.aboutNoticeBn
            else "সম্পূর্ণ এপ্লিকেশন টি কাজ করছে ওয়েবসাইট/ব্লগার এবং adsterra বিজ্ঞাপন থেকে রেভিনিউ অর্জন করার জন্য। আসুন একে অপরের সাথে একসাথে মিলে কাজ করি সামনের দিকে এগিয়ে যাই। এছাড়াও আপনার ওয়েবসাইট/ব্লগার এবং ইউটিউব ভিডিও ওয়াচ টাইম ও ভিউ প্রমোশন চালানোর জন্য অ্যাপ্লিকেশনটি খুবই কার্যকর।"
        )
    }
    var aboutNoticeEn by remember {
        mutableStateOf(
            if (settings.aboutNoticeEn.isNotBlank()) settings.aboutNoticeEn
            else "GlobalSEO Engine is designed for website/blogger traffic monetization and Adsterra revenue growth. Let's work together to grow our audience. This app is highly effective for promoting your website/blogger visitors and YouTube watch time and views."
        )
    }
    var apiKey by remember { mutableStateOf(settings.firebaseApiKey) }
    var projectId by remember { mutableStateOf(settings.firebaseProjectId) }
    var appId by remember { mutableStateOf(settings.firebaseAppId) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 30.dp)
    ) {
        // Initialize Firestore Collections Button
        Button(
            onClick = {
                viewModel.initializeFirestore { success ->
                    val msg = if (success) Strings.get("firestore_init_success", language) else "Collections initialized"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("init_firestore_collections_button")
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Strings.get("init_firestore_btn", language),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "General App Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = adminEmail,
            onValueChange = { adminEmail = it },
            label = { Text(Strings.get("admin_email_setting", language)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = sendMoneyNumber,
            onValueChange = { sendMoneyNumber = it },
            label = { Text("Deposit Send Money Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = developerName,
            onValueChange = { developerName = it },
            label = { Text(Strings.get("developer_name_setting", language)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = facebookUrl,
            onValueChange = { facebookUrl = it },
            label = { Text("Facebook Profile Link") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = whatsappNumber,
            onValueChange = { whatsappNumber = it },
            label = { Text("WhatsApp Contact Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = aboutNoticeBn,
            onValueChange = { aboutNoticeBn = it },
            label = { Text(if (language == AppLanguage.BANGLA) "এবাউট সেকশন নোটিশ (বাংলা)" else "About Section Notice (Bangla)") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = aboutNoticeEn,
            onValueChange = { aboutNoticeEn = it },
            label = { Text(if (language == AppLanguage.BANGLA) "এবাউট সেকশন নোটিশ (English)" else "About Section Notice (English)") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Firebase API & Firestore Config",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("Firebase API Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = projectId,
            onValueChange = { projectId = it },
            label = { Text("Firebase Project ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = appId,
            onValueChange = { appId = it },
            label = { Text("Firebase App ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Settings Button
        Button(
            onClick = {
                val updated = settings.copy(
                    adminEmail = adminEmail.trim(),
                    developerName = developerName.trim(),
                    facebookUrl = facebookUrl.trim(),
                    whatsappNumber = whatsappNumber.trim(),
                    sendMoneyNumber = sendMoneyNumber.trim(),
                    aboutNoticeBn = aboutNoticeBn.trim(),
                    aboutNoticeEn = aboutNoticeEn.trim(),
                    firebaseApiKey = apiKey.trim(),
                    firebaseProjectId = projectId.trim(),
                    firebaseAppId = appId.trim()
                )
                viewModel.updateSettings(updated)
                Toast.makeText(context, "Settings updated successfully", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Strings.get("save", language), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusBadge(status: String, language: AppLanguage) {
    val color = when (status) {
        "approved" -> EmeraldSuccess
        "rejected" -> MaterialTheme.colorScheme.error
        else -> GoldCoin
    }
    val label = when (status) {
        "approved" -> Strings.get("status_approved", language)
        "rejected" -> Strings.get("status_rejected", language)
        else -> Strings.get("status_pending", language)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
