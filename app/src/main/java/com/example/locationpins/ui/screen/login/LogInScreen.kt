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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationpins.data.repository.UserRepository
import com.example.locationpins.ui.screen.gallery.GalleryStep

enum class LoginMode {
    Login,
    Register,
    ForgotPassword
}

@Composable
fun LoginView(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(LoginMode.Login) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when {
            uiState.isLoading == true -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            else ->
                when (currentStep) {
                    LoginMode.Login -> LoginScreen(
                        onLoginSuccess = onLoginSuccess,
                        onRegisterClick = {
                            currentStep = LoginMode.Register
                            viewModel.reset()
                        },
                        onForgotPasswordClick = { currentStep = LoginMode.ForgotPassword },
                        viewModel = viewModel
                    )

                    LoginMode.Register -> RegisterScreen(
                        onRegisterSuccess = {
                            currentStep = LoginMode.Login
                            viewModel.reset()
                        },
                        viewModel = viewModel,
                        onLoginClick = {
                            currentStep = LoginMode.Login
                            viewModel.reset()
                        }
                    )

                    LoginMode.ForgotPassword -> TODO()
                }
        }
    }
}

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(greeting = "Chào mừng trở lại", content = "Đăng nhập để tiếp tục")

        Spacer(Modifier.height(32.dp))

        TextFieldNotHidden(
            info = uiState.username,
            onValueChange = { viewModel.enterUsername(it) },
            content = "Tên đăng nhập"
        )

        Spacer(Modifier.height(18.dp))

        TextFieldCanHidden(
            password = uiState.password,
            isVisble = uiState.isPasswordVisible,
            onValueChange = { viewModel.enterPassword(it) },
            setVisible = { viewModel.togglePasswordVisibility() },
            content = "Mật khẩu"
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

            )
        }
        Spacer(Modifier.height(8.dp))

        ButtonLogIn(
            onLoginClick = {
                viewModel.login() { success ->
                    if (success) {
                        onLoginSuccess()
                    }
                }
            },
            content = "Đăng nhập"
        )

        Spacer(Modifier.height(24.dp))

        RowNavigation(
            content = "Chưa có tài khoản ?",
            nameButton = "Đăng kí",
            onClick = onRegisterClick
        )


    }

}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
        Header(
            greeting = "Đăng kí tài khoản",
            content = "Tạo tài khoản mới để bắt đầu",
            iconSize = 20.dp
        )
        Spacer(Modifier.height(10.dp))
        TextFieldNotHidden(
            content = "Tên đăng nhập",
            onValueChange = { viewModel.enterUsername(it) },
            info = uiState.username
        )
        Spacer(Modifier.height(10.dp))
        TextFieldNotHidden(
            content = "Họ tên",
            onValueChange = { viewModel.enterName(it) },
            info = uiState.name
        )
        Spacer(Modifier.height(10.dp))
        TextFieldNotHidden(
            content = "Email",
            onValueChange = { viewModel.enterEmail(it) },
            info = uiState.email
        )
        Spacer(Modifier.height(10.dp))
        TextFieldCanHidden(
            content = "Mật khẩu",
            onValueChange = { viewModel.enterPassword(it) },
            isVisble = uiState.isPasswordVisible,
            setVisible = { viewModel.togglePasswordVisibility() },
            password = uiState.password
        )
        Spacer(Modifier.height(10.dp))
        TextFieldCanHidden(
            content = "Nhập lại mật khẩu",
            onValueChange = { viewModel.enterConfirmPassword(it) },
            isVisble = uiState.isPasswordVisible,
            setVisible = { viewModel.togglePasswordVisibility() },
            password = uiState.confirmPassword
        )
        Spacer(Modifier.height(10.dp))
        ButtonLogIn(onLoginClick = {
            viewModel.register { success ->
                if (success) {
                    onRegisterSuccess()
                }
            }
        }, content = "Đăng kí")
        Spacer(Modifier.height(5.dp))
        RowNavigation(content = "Đã có tài khoản", nameButton = "Đăng nhập", onClick = onLoginClick)
    }
}

@Composable
fun Header(greeting: String, content: String, iconSize: Dp = 100.dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {

        Image(
            painter = painterResource(id = R.drawable.pin_red),
            contentDescription = "Pin đỏ",
            modifier = Modifier.size(iconSize)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = greeting,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}


@Composable
fun TextFieldNotHidden(info: String, content: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = info,
        onValueChange = onValueChange,
        placeholder = { Text("Nhập tên đăng nhập") },
        label = { Text(text = content) },
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
fun TextFieldCanHidden(
    password: String,
    isVisble: Boolean,
    content: String,
    onValueChange: (String) -> Unit,
    setVisible: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onValueChange,
        placeholder = { Text("Nhập mật khẩu") },
        label = { Text(text = content) },
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
fun ButtonLogIn(onLoginClick: () -> Unit, content: String) {
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
        Text(text = content, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }

}

@Composable
fun RowNavigation(content: String, nameButton: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(onClick = onClick) {
            Text(text = nameButton, color = Color(0xFF2F68FF), fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LocationSocialTheme {
        LoginView(
            onLoginSuccess = {})
    }
}