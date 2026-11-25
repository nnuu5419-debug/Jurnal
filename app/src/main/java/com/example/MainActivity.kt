package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AccessStatus
import com.example.ui.MainViewModel
import com.example.ui.navigation.AuthenticatedShell
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.PendingAccessScreen
import com.example.ui.theme.JalalAbadCollegeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JalalAbadCollegeTheme {
                val viewModel: MainViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                when {
                    !uiState.isInitialized -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.currentUser == null -> {
                        AuthScreen(
                            allUsers = uiState.allUsers,
                            authError = uiState.authError,
                            onRegisterOrSignIn = viewModel::registerOrSignIn,
                            onSwitchAccount = viewModel::switchAccount,
                            onClearError = viewModel::clearAuthError
                        )
                    }
                    uiState.currentUser?.status != AccessStatus.APPROVED -> {
                        val user = uiState.currentUser!!
                        PendingAccessScreen(
                            user = user,
                            onSignOut = viewModel::signOut
                        )
                    }
                    else -> {
                        val user = uiState.currentUser!!
                        AuthenticatedShell(
                            currentUser = user,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
