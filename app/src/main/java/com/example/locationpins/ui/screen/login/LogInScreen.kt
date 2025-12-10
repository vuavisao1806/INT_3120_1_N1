package com.example.locationpins.ui.screen.login

import com.example.locationpins.R
import androidx.compose.foundation.Image
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.locationpins.ui.theme.LocationSocialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationpins.data.repository.UserRepository

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val viewModel = remember {
        LoginViewModel(userRepository = UserRepository())
    }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header()

        Spacer(Modifier.height(32.dp))

        TextFieldUserName(
            userName = uiState.username,
            onValueChange = { viewModel.enterUsername(it) })

        Spacer(Modifier.height(18.dp))

        TextFieldPassword(
            password = uiState.password,
            isVisble = uiState.isPasswordVisible,
            onValueChange = { viewModel.enterPassword(it) },
            setVisible = { viewModel.togglePasswordVisibility() }
        )


        Spacer(Modifier.height(10.dp))

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Quên mật khẩu?",
                color = Color(0xFF2F68FF),
                fontSize = 14.sp
            )
        }
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(8.dp))

        ButtonLogIn(onLoginClick = {
            viewModel.login() { success ->
                if (success) {
                    onLoginSuccess()
                }
            }
        })

        Spacer(Modifier.height(24.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chưa có tài khoản?",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onRegisterClick) {
                Text("Đăng ký", color = Color(0xFF2F68FF), fontSize = 14.sp)
            }
        }

    }

}

@Composable
fun Header() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Image(
            painter = painterResource(id = R.drawable.pin_red),
            contentDescription = "Pin đỏ",

            )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Chào mừng trở lại",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Đăng nhập để tiếp tục",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}


@Composable
fun TextFieldUserName(userName: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = userName,
        onValueChange = onValueChange,
        placeholder = { Text("Nhập tên đăng nhập") },
        label = { Text("Tên đăng nhập") },
        leadingIcon = { Icon(Icons.Default.Person, null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4D8EFF),
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )
}

@Composable
fun TextFieldPassword(
    password: String,
    isVisble: Boolean,
    onValueChange: (String) -> Unit,
    setVisible: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onValueChange,
        placeholder = { Text("Nhập mật khẩu") },
        label = { Text("Mật khẩu") },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (isVisble)
            VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { setVisible() }) {
                Icon(Icons.Outlined.Visibility, null)
            }

        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4D8EFF),
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )
}

@Composable
fun ButtonLogIn(onLoginClick: () -> Unit) {
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2F68FF)
        )
    ) {
        Text(text = "Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }

}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LocationSocialTheme {
        LoginScreen(
            onRegisterClick = {},
            onForgotPasswordClick = {},
            onLoginSuccess = {})
    }
}