package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AccessStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.ui.components.StatusPill
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusExcused
import com.example.ui.theme.StatusExcusedContainer
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentContainer

@Composable
fun AuthScreen(
    allUsers: List<UserAccount>,
    authError: String?,
    onRegisterOrSignIn: (email: String, fullName: String, role: UserRole) -> Unit,
    onSwitchAccount: (String) -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STAROSTA) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Campus Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(modifier = Modifier.height(190.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_college_banner),
                            contentDescription = "Жалал-Абад колледжи",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color(0xFF083A2F).copy(alpha = 0.90f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(8.dp).size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Жалал-Абад колледжи",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Жумалык катышуу жана аналитика порталы",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Gmail Registration & Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Gmail аркылуу кирүү / Катталуу",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Староста же Администратор катары @gmail.com почтаңыз менен катталыңыз. Старосталар администратор доступ бергенден кийин иштей алат.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (authError != null) onClearError()
                            },
                            label = { Text("Gmail дарегиңиз") },
                            placeholder = { Text("misal@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Default.AlternateEmail, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gmail_email_input")
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                if (authError != null) onClearError()
                            },
                            label = { Text("Аты-жөнүңүз (жаңы катталуу болсо)") },
                            placeholder = { Text("Асанов Бектур") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gmail_name_input")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Ролду тандаңыз:",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == UserRole.STAROSTA,
                                    onClick = { selectedRole = UserRole.STAROSTA },
                                    label = { Text("Староста") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.weight(1f).testTag("role_starosta_chip")
                                )
                                FilterChip(
                                    selected = selectedRole == UserRole.ADMIN,
                                    onClick = { selectedRole = UserRole.ADMIN },
                                    label = { Text("Админ") },
                                    leadingIcon = {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.weight(1f).testTag("role_admin_chip")
                                )
                            }
                        }

                        if (!authError.isNullOrBlank()) {
                            Surface(
                                color = StatusAbsentContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = authError,
                                    color = StatusAbsent,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { onRegisterOrSignIn(email, fullName, selectedRole) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("gmail_sign_in_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gmail менен кирүү")
                        }
                    }
                }

                // Quick Saved Accounts Switcher
                if (allUsers.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Түзмөктөгү катталган Gmail аккаунттар",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            allUsers.forEach { account ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { onSwitchAccount(account.email) }
                                        .testTag("quick_account_${account.id}"),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = account.fullName.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = account.fullName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = account.email,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        val (bg, fg) = when (account.status) {
                                            AccessStatus.APPROVED -> StatusPresentContainer to StatusPresent
                                            AccessStatus.PENDING -> StatusExcusedContainer to StatusExcused
                                            AccessStatus.REVOKED -> StatusAbsentContainer to StatusAbsent
                                        }
                                        StatusPill(
                                            text = "${account.role.labelKy} • ${account.status.labelKy}",
                                            containerColor = bg,
                                            contentColor = fg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Offline Local Storage Notice
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Маалыматтар түзмөктүн ичиндеги локалдык базада (Room) сакталат. Булут синхронизациясы (Firebase) кошулган эмес.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingAccessScreen(
    user: UserAccount,
    onSignOut: () -> Unit
) {
    val isRevoked = user.status == AccessStatus.REVOKED
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 460.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = if (isRevoked) StatusAbsentContainer else StatusExcusedContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRevoked) Icons.Default.Block else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (isRevoked) StatusAbsent else StatusExcused,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = if (isRevoked) "Доступ жабылган" else "Администратордун уруксаты күтүлүүдө",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${user.fullName} (${user.email})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (isRevoked) {
                        "Сиздин аккаунтуңузга доступ убактылуу токтотулган. Колледждин администраторуна кайрылыңыз."
                    } else {
                        "Сиздин Gmail аккаунтуңуз ийгиликтүү катталды. Администратор «Админ панель» аркылуу доступ берип, группа бекиткенден кийин жоктоо жүргүзө аласыз."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pending_sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Башка Gmail аккаунтка кирүү")
                }
            }
        }
    }
}
