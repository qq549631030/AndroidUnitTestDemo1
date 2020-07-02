package cn.hx.demo.presenter

import cn.hx.demo.model.Callback
import cn.hx.demo.model.ILoginModel
import cn.hx.demo.view.ILoginView
import java.lang.Exception

class LoginPresenter(private val loginModel: ILoginModel, val loginView: ILoginView) :
    ILoginPresenter {

    override fun login() {
        loginModel.login(
            loginView.getUserName(),
            loginView.getPassword(),
            object : Callback {
                override fun onSuccess(result: String) {
                    loginView.showToast(result)
                }

                override fun onError(e: Exception) {
                    loginView.showToast(e.message.toString())
                }
            })
    }
}