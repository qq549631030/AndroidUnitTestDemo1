package cn.hx.demo.presenter

import cn.hx.demo.model.Callback
import cn.hx.demo.model.ILoginModel
import cn.hx.demo.view.ILoginView
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Test

class LoginPresenterTest {

    @Test
    fun loginSuccess() {
        //mock一个ILoginModel
        val mockLoginModel = mock<ILoginModel>() {
            //login方法实现
            on { login(any(), any(), any()) } doAnswer {
                //获取方法的第三个参数Callback,调用Callback的onSuccess方法
                it.getArgument<Callback>(2).onSuccess("登录成功")
            }
        }
        //mock一个ILoginView
        val mockLoginView = mock<ILoginView>() {
            //getUserName方法实现
            on { getUserName() } doReturn "huangx"
            //getPassword方法实现
            on { getPassword() } doReturn "123456"
        }

        //使用mock的ILoginModel和ILoginView创建LoginPresenter
        val loginPresenter = LoginPresenter(mockLoginModel, mockLoginView)
        //调用LoginPresenter的登录方法
        loginPresenter.login()

        //校验mock的对象指定方法被调用过
        verify(mockLoginView).getUserName()
        verify(mockLoginView).getPassword()
        verify(mockLoginModel).login(eq("huangx"), eq("123456"), any())
        verify(mockLoginView).showToast(eq("登录成功"))
    }

    @Test
    fun loginFail() {
        val mockLoginModel = mock<ILoginModel>() {
            on { login(any(), any(), any()) } doAnswer {
                it.getArgument<Callback>(2).onError(Exception("用户名或密码错误"))
            }
        }
        val mockLoginView = mock<ILoginView>() {
            on { getUserName() } doReturn "huangx"
            on { getPassword() } doReturn "1234567"
        }

        val loginPresenter = LoginPresenter(mockLoginModel, mockLoginView)

        loginPresenter.login()

        verify(mockLoginView).getUserName()
        verify(mockLoginView).getPassword()
        verify(mockLoginModel).login(eq("huangx"), eq("1234567"), any())
        verify(mockLoginView).showToast(eq("用户名或密码错误"))
    }

    @Ignore
    @Test
    fun loginSuccessByMockk() {
        //mock一个ILoginModel
        val mockLoginModel = mockk<ILoginModel>(relaxed = true) {
            every { login(any(), any(), any()) } answers {
                //获取方法的第三个参数Callback,调用Callback的onSuccess方法
                arg<Callback>(2).onSuccess("登录成功")
            }
        }
        //mock一个ILoginView
        val mockLoginView = mockk<ILoginView>(relaxed = true) {
            every { getUserName() } returns "huangx"
            every { getPassword() } returns "123456"
        }
        //使用mock的ILoginModel和ILoginView创建LoginPresenter
        val loginPresenter = LoginPresenter(mockLoginModel, mockLoginView)
        //调用LoginPresenter的登录方法
        loginPresenter.login()
        //校验mock的对象指定方法被调用过
        io.mockk.verify { mockLoginView.getUserName() }
        io.mockk.verify { mockLoginView.getPassword() }
        io.mockk.verify { mockLoginModel.login("huangx", "123456", any()) }
        io.mockk.verify { mockLoginView.showToast("登录成功") }
    }
}