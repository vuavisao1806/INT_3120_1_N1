package com.example.locationpins

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.locationpins.data.repository.UserRepository
import com.example.locationpins.ui.screen.login.LoginScreen
import com.example.locationpins.ui.screen.login.LoginViewModel
import com.example.locationpins.ui.screen.login.RegisterScreen
import com.example.locationpins.ui.theme.LocationSocialTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    /**
     * Rule để thiết lập môi trường test cho Compose.
     * Nó cung cấp các hàm để setContent và tương tác với các node trên UI.
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: LoginViewModel
    private lateinit var mockRepository: UserRepository

    @Before
    fun setup() {
        // Mock Repository với relaxed = true để trả về giá trị mặc định cho các hàm chưa được khai báo
        mockRepository = mockk(relaxed = true)

        // Khởi tạo ViewModel với Repository giả
        viewModel = LoginViewModel(userRepository = mockRepository)
    }

    /**
     * Test 1: Kiểm tra xem các thành phần UI cơ bản có hiển thị đúng không.
     */
    @Test
    fun loginScreenDisplaysAllComponents() {
        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = {},
                    onForgotPasswordClick = {},
                    onLoginSuccess = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Chào mừng trở lại").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đăng nhập để tiếp tục").assertIsDisplayed()

        composeTestRule.onNodeWithText("Tên đăng nhập").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mật khẩu").assertIsDisplayed()

        composeTestRule.onNodeWithText("Đăng nhập").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quên mật khẩu?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đăng kí").assertIsDisplayed()
    }

    /**
     * Test 2: Kiểm tra khả năng nhập liệu.
     * Khi người dùng gõ phím, UI phải cập nhật giá trị tương ứng.
     */
    @Test
    fun loginScreenIsEnterInput() {
        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = {},
                    onForgotPasswordClick = {},
                    onLoginSuccess = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Tên đăng nhập")
            .performTextInput("nguoidung123")
        composeTestRule.onNodeWithText("nguoidung123").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("matkhau123")


        composeTestRule.onNodeWithText("matkhau123").assertExists()
    }

    /**
     * Test 3: Kiểm tra hiển thị lỗi khi bỏ trống thông tin.
     * Logic này nằm trong hàm login() của ViewModel:
     * if (userName.isBlank() || password.isBlank()) -> errorMessage = "Vui lòng nhập tên đăng nhập và mật khẩu"
     */
    @Test
    fun loginScreenEmpty() {
        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = {},
                    onForgotPasswordClick = {},
                    onLoginSuccess = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Đăng nhập").performClick()
        composeTestRule.onNodeWithText("Vui lòng nhập tên đăng nhập và mật khẩu").assertIsDisplayed()
    }

    /**
     * Test 4: Kiểm tra điều hướng sang màn hình Đăng ký.
     * Khi bấm nút "Đăng kí", callback onRegisterClick phải được kích hoạt.
     */
    @Test
    fun loginScreenNavigationToRegister() {
        var navigatedToRegister = false

        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = { navigatedToRegister = true },
                    onForgotPasswordClick = {},
                    onLoginSuccess = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Đăng kí")
            .performScrollTo()
            .performClick()


        assert(navigatedToRegister) { "Callback chuyển trang đăng ký chưa được gọi!" }
    }

    /**
     * Test 5
     * Nhập ô trống
     */
    @Test
    fun loginEmptyInput() {

        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = {},
                    onForgotPasswordClick = {},
                    onLoginSuccess = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Đăng nhập").performClick()
        composeTestRule.onNodeWithText("Vui lòng nhập tên đăng nhập và mật khẩu")
            .assertIsDisplayed()
    }

    /**
     * Test 6
     * Kiểm tra khi đăng nhập sai
     */
    @Test
    fun loginFail() {
        coEvery { mockRepository.login(any(), any()) } returns mockk(relaxed = true) {
            coEvery { success } returns false
        }

        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = {},
                    onForgotPasswordClick = {},
                    onLoginSuccess = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Tên đăng nhập").performTextInput("user_sai")
        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("pass_sai")
        composeTestRule.onNodeWithText("Đăng nhập").performClick()


        composeTestRule.onNodeWithText("Sai tên đăng nhập hoặc mật khẩu!")
            .assertIsDisplayed()
    }

    /**
     * Test 7
     * Đăng nhập đúng
     */
    @Test
    fun loginSuccess() {
        coEvery { mockRepository.login("user_dung", "pass_dung") } returns mockk(relaxed = true) {
            coEvery { success } returns true
            coEvery { user } returns mockk(relaxed = true)
        }

        var isLoginSuccessCalled = false

        composeTestRule.setContent {
            LocationSocialTheme {
                LoginScreen(
                    onRegisterClick = {},
                    onForgotPasswordClick = {},
                    onLoginSuccess = { isLoginSuccessCalled = true },
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Tên đăng nhập").performTextInput("user_dung")
        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("pass_dung")
        composeTestRule.onNodeWithText("Đăng nhập").performClick()

        assert(isLoginSuccessCalled) { "Lẽ ra phải chuyển màn hình nhưng chưa thấy gọi callback!" }
    }

    /**
     * Test 8
     * Nhập thiếu thông tin đăng kí
     */
    @Test
    fun registerLackInfo() {
        composeTestRule.setContent {
            LocationSocialTheme {
                RegisterScreen(
                    onRegisterSuccess = {},
                    onLoginClick = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Tên đăng nhập").performTextInput("user_test")
        composeTestRule.onNodeWithText("Đăng kí").performClick()
        composeTestRule.onNodeWithText("Vui lòng điền đầy đủ thông tin").assertIsDisplayed()
    }

    /**
     * Test 9
     * Nhập đầy đủ thông tin
     */
    @Test
    fun registerSuccess() {
        coEvery { mockRepository.register(any(), any(), any(), any()) } returns mockk(relaxed = true) {
            every { registerSuccess } returns true
            every { userNameTaken } returns false
            every { userEmailTaken } returns false
        }

        var isRegisterSuccessCalled = false

        composeTestRule.setContent {
            LocationSocialTheme {
                RegisterScreen(
                    onRegisterSuccess = { isRegisterSuccessCalled = true }, // Gắn cờ
                    onLoginClick = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Tên đăng nhập").performTextInput("user_moi")
        composeTestRule.onNodeWithText("Họ tên").performTextInput("Nguyen Van A")
        composeTestRule.onNodeWithText("Email").performTextInput("test@gmail.com")


        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("123456")
        composeTestRule.onNodeWithText("Nhập lại mật khẩu").performTextInput("123456")

        composeTestRule.onNodeWithText("Đăng kí").performClick()


        assert(isRegisterSuccessCalled) { "Callback đăng ký thành công chưa được gọi" }
    }

    /**
     * Test 10
     * User name đăng kí đã tồn tại
     */
    @Test
    fun registerDuplicationUserName() {
        // Setup: Giả lập API trả về lỗi trùng Username
        coEvery { mockRepository.register(any(), any(), any(), any()) } returns mockk(relaxed = true) {
            every { userNameTaken } returns true
        }

        composeTestRule.setContent {
            LocationSocialTheme {
                RegisterScreen(
                    onRegisterSuccess = {},
                    onLoginClick = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Tên đăng nhập").performTextInput("user_cu")
        composeTestRule.onNodeWithText("Họ tên").performTextInput("Nguyen Van B")
        composeTestRule.onNodeWithText("Email").performTextInput("new@gmail.com")
        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("123456")
        composeTestRule.onNodeWithText("Nhập lại mật khẩu").performTextInput("123456")

        composeTestRule.onNodeWithText("Đăng kí").performClick()


        composeTestRule.onNodeWithText("Người dùng đã tồn tại").assertIsDisplayed()
    }

    /**
     * Test 11
     * Email đăng kí đã tồn tại
     */
    @Test
    fun registerDuplicationEmail() {

        coEvery { mockRepository.register(any(), any(), any(), any()) } returns mockk(relaxed = true) {
            every { userEmailTaken } returns true
        }

        composeTestRule.setContent {
            LocationSocialTheme {
                RegisterScreen(
                    onRegisterSuccess = {},
                    onLoginClick = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Tên đăng nhập").performTextInput("user_moi_2")
        composeTestRule.onNodeWithText("Họ tên").performTextInput("Nguyen Van C")
        composeTestRule.onNodeWithText("Email").performTextInput("email_cu@gmail.com")
        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("123456")
        composeTestRule.onNodeWithText("Nhập lại mật khẩu").performTextInput("123456")

        composeTestRule.onNodeWithText("Đăng kí").performClick()


        composeTestRule.onNodeWithText("Email đã tồn tại").assertIsDisplayed()
    }
}