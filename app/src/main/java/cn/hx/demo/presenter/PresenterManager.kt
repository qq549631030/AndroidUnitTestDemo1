package cn.hx.demo.presenter

import cn.hx.demo.model.ModelManager
import cn.hx.demo.view.ILoginView

object PresenterManager {
    fun provideLoginPresenter(view: ILoginView): ILoginPresenter {
        return LoginPresenter(ModelManager.provideLoginModel(), view)
    }
}