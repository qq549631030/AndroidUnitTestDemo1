package cn.hx.demo.view

interface ILoginView {
    fun getUserName(): String

    fun getPassword(): String

    fun showToast(msg: String)
}