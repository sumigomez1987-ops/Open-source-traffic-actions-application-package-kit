package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.navigateTo(Screen.DASHBOARD) }
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                        contentDescription = "Logo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = Strings.get("app_title", language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Traffic & Reward Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Right side: Language Switcher, Coin Badge, Three-dot Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language Switcher Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            val next = if (language == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
                            viewModel.switchLanguage(next)
                        }
                        .testTag("language_switch_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "বাংলা" else "EN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Coin Badge
                if (currentUser != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GoldCoin.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.navigateTo(Screen.DEPOSIT) }
                            .testTag("coins_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🪙",
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentUser?.coins ?: 0}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldCoin
                            )
                        }
                    }
                }

                // Three-dot Menu in Corner
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("three_dot_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .width(220.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        MenuItem(
                            icon = Icons.Default.Dashboard,
                            title = Strings.get("dashboard", language),
                            selected = currentScreen == Screen.DASHBOARD
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.DASHBOARD)
                        }

                        MenuItem(
                            icon = Icons.Default.Work,
                            title = Strings.get("remote_job", language),
                            selected = currentScreen == Screen.REMOTE_JOB
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.REMOTE_JOB)
                        }

                        MenuItem(
                            icon = Icons.Default.AddCard,
                            title = Strings.get("deposit", language),
                            selected = currentScreen == Screen.DEPOSIT
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.DEPOSIT)
                        }

                        MenuItem(
                            icon = Icons.Default.Payments,
                            title = Strings.get("withdrawal", language),
                            selected = currentScreen == Screen.WITHDRAWAL
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.WITHDRAWAL)
                        }

                        MenuItem(
                            icon = Icons.Default.Person,
                            title = Strings.get("profile", language),
                            selected = currentScreen == Screen.PROFILE
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.PROFILE)
                        }

                        MenuItem(
                            icon = Icons.Default.Share,
                            title = Strings.get("referral", language),
                            selected = currentScreen == Screen.REFERRAL
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.REFERRAL)
                        }

                        MenuItem(
                            icon = Icons.Default.Info,
                            title = Strings.get("about", language),
                            selected = currentScreen == Screen.ABOUT
                        ) {
                            menuExpanded = false
                            viewModel.navigateTo(Screen.ABOUT)
                        }

                        // Admin Access
                        if (viewModel.isAdmin) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            MenuItem(
                                icon = Icons.Default.AdminPanelSettings,
                                title = Strings.get("admin", language),
                                selected = currentScreen == Screen.ADMIN,
                                isHighlight = true
                            ) {
                                menuExpanded = false
                                viewModel.navigateTo(Screen.ADMIN)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        MenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = Strings.get("sign_out", language),
                            selected = false,
                            isDestructive = true
                        ) {
                            menuExpanded = false
                            viewModel.signOut()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    isHighlight: Boolean = false,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = when {
                        isDestructive -> MaterialTheme.colorScheme.error
                        isHighlight -> MaterialTheme.colorScheme.primary
                        selected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected || isHighlight) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isDestructive -> MaterialTheme.colorScheme.error
                        isHighlight -> MaterialTheme.colorScheme.primary
                        selected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                else Modifier
            )
    )
}
