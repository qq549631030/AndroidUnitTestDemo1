package cn.hx.demo.model

import org.junit.Assert.*
import org.junit.Test
import java.lang.Exception

class LoginModelTest {

    @Test
    fun login_Success() {
        var resultStr: String? = null
        val loginModel = LoginModel()
        loginModel.login("huangx", "123456", object : Callback {
            override fun onSuccess(result: String) {
                resultStr = result
            }

            override fun onError(e: Exception) {
                resultStr = e.message.toString()
            }
        })
        assertEquals("登录成功", resultStr)
    }

    @Test
    fun login_Fail() {
        var resultStr: String? = null
        val loginModel = LoginModel()
        loginModel.login("huangx", "1234567", object : Callback {
            override fun onSuccess(result: String) {
                resultStr = result
            }

            override fun onError(e: Exception) {
                resultStr = e.message.toString()
            }
        })
        assertEquals("用户名或密码错误", resultStr)
    }
}