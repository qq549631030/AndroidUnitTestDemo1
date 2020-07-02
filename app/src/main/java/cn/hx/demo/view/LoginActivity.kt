package cn.hx.demo.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.hx.demo.R
import cn.hx.demo.presenter.ILoginPresenter
import cn.hx.demo.presenter.PresenterManager
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), ILoginView {

    lateinit var loginPresenter: ILoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginPresenter = PresenterManager.provideLoginPresenter(this)
        btn_login.setOnClickListener {
            loginPresenter.login()
        }
    }

    override fun getUserName(): String {
        return et_username.text.toString()
    }

    override fun getPassword(): String {
        return et_password.text.toString()
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}