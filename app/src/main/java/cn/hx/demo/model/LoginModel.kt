package cn.hx.demo.model

import java.lang.Exception

class LoginModel : ILoginModel {
    override fun login(userName: String, password: String, callback: Callback) {

        if (userName == "huangx" && password == "123456") {
            callback.onSuccess("登录成功")
        } else {
            callback.onError(Exception("用户名或密码错误"))
        }
    }
}