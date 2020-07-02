package cn.hx.demo.model

interface ILoginModel {
    fun login(userName: String, password: String, callback: Callback)
}