package cn.hx.demo.model

object ModelManager {
    fun provideLoginModel(): ILoginModel {
        return LoginModel()
    }
}