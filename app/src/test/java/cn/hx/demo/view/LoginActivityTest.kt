package cn.hx.demo.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import cn.hx.demo.R
import cn.hx.demo.model.Callback
import cn.hx.demo.model.ILoginModel
import cn.hx.demo.model.ModelManager
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class LoginActivityTest {

    @Before
    fun setUp() {
        //mock掉ModelManager(没有mock具体方法的话默认还是原始方法)
        mockkObject(ModelManager)
    }

    @After
    fun tearDown() {
        //清空所有mock
        unmockkAll()
    }

    @Test
    fun loginSuccess() {
        //mock一个ILoginModel
        val mockLoginModel = mockk<ILoginModel>() {
            every { login(any(), any(), any()) } answers {
                //获取方法的第三个参数Callback,调用Callback的onSuccess方法
                arg<Callback>(2).onSuccess("登录成功")
            }
        }
        //provideLoginModel方法返回mockLoginModel
        every { ModelManager.provideLoginModel() } returns mockLoginModel
        //启动LoginActivity
        val activityController = Robolectric.buildActivity(LoginActivity::class.java)
        activityController.setup()
        //用户名输入框判断
        onView(withId(R.id.et_username)).check(matches(withText("")))
        onView(withId(R.id.et_username)).check(matches(withHint("username")))
        //输入用户名
        onView(withId(R.id.et_username)).perform(typeText("huangx"))
            .check(matches(withText("huangx")))
        //密码输入框判断
        onView(withId(R.id.et_password)).check(matches(withText("")))
        onView(withId(R.id.et_password)).check(matches(withHint("password")))
        //输入密码
        onView(withId(R.id.et_password)).perform(typeText("123456"))
            .check(matches(withText("123456")))
        //登录按钮判断
        onView(withId(R.id.btn_login)).check(matches(withText("LOGIN")))
        //登录按钮点击
        onView(withId(R.id.btn_login)).perform(click())
        //验证调用了mockLoginModel的login方法
        verify { mockLoginModel.login("huangx", "123456", any()) }
        //验证弹了toast
        assertEquals("登录成功", ShadowToast.getTextOfLatestToast())
        activityController.destroy()
    }

    @Test
    fun loginFail() {
        val mockLoginModel = mockk<ILoginModel>() {
            every { login(any(), any(), any()) } answers {
                arg<Callback>(2).onError(Exception("用户名或密码错误"))
            }
        }
        every { ModelManager.provideLoginModel() } returns mockLoginModel
        val activityController = Robolectric.buildActivity(LoginActivity::class.java)
        activityController.setup()
        onView(withId(R.id.et_username)).perform(typeText("huangx"))
        onView(withId(R.id.et_password)).perform(typeText("123456"))
        onView(withId(R.id.btn_login)).perform(click())
        verify { mockLoginModel.login("huangx", "123456", any()) }
        assertEquals("用户名或密码错误", ShadowToast.getTextOfLatestToast())
        activityController.destroy()
    }
}