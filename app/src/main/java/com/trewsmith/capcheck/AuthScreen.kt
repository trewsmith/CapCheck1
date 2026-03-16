package com.trewsmith.capcheck

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsState()
    val error by authViewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSignUp) "Create Account" else "Sign In",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isSignUp) authViewModel.signUp(email, password)
                else authViewModel.signIn(email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "Sign Up" else "Sign In")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}