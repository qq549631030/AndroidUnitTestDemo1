Android单元测试主要包含两种：

1. 本地测试

   本地测试代码在test目录下，只需要在电脑上就可以运行

2. 仪器化测试

   仪器化测试代码在androidTest目录下，需要在手机上运行

本系列文章所说的单元测试讲的就是本地测试。

个人觉得MVP模式是比较适合做单元测试的，本系列也都是建立在MVP模式下，MVP相关内容可以参数我的其它相关文章。



从AndroidStudio新建的项目默认已经建立了最基本的单元测试依赖junit

```groovy
testImplementation 'junit:junit:4.13'
```

并且在test目录下建了一个最基本的单元测试类

```kotlin
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

单元测试用到的依赖使用`testImplementation` 

下面用最简单的MVP来实验下

#### Model层

```kotlin
//ILoginModel
interface ILoginModel {
    fun login(userName: String, password: String, callback: Callback<String>)
}
//LoginModel
class LoginModel : ILoginModel {
    override fun login(userName: String, password: String, callback: Callback<String>) {
        if (userName == "huangx" && password == "123456") {
            callback.onSuccess("登录成功")
        } else {
            callback.onError(Exception("用户名或密码错误"))
        }
    }
}
```

单元测试测的是类，接口是没有测试必要的，我们对LoginModel测试

```kotlin
class LoginModelTest {

    @Test
    fun login_Success() {
        var resultStr: String? = null
        val loginModel = LoginModel()
        loginModel.login("huangx", "123456", object : Callback<String> {
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
        loginModel.login("huangx", "1234567", object : Callback<String> {
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
```

LoginModel逻辑非常简单，纯java而且内部也没有依赖其它类，只用junit的assert*系列方法就够了

#### Presenter层

```kotlin
//ILoginPresenter
interface ILoginPresenter {
    fun login()
}
//LoginPresenter
class LoginPresenter(private val loginModel: ILoginModel, val loginView: ILoginView) :
    ILoginPresenter {

    override fun login() {
        loginModel.login(
            loginView.getUserName(),
            loginView.getPassword(),
            object : Callback<String> {
                override fun onSuccess(result: String) {
                    loginView.showToast(result)
                }

                override fun onError(e: Exception) {
                    loginView.showToast(e.message.toString())
                }
            })
    }
}
```

LoginPresenter与LoginModel不同，它使用到的ILoginModel与ILoginView两个外部类。这里要说到单元测试的要遵守的一个原则，仅实例化当前测试类的对象，其它依赖类都mock掉。mock是什么意思呢，就是在测试过程中，对于某些不容易构造或者不容易获取的对象，用一个虚拟的对象来创建以便测试。

Mock最常用的框架就是Mockito,但是因为我们这里用的语言是kotlin，用Mockito的话any()会反回null，对于kotlin非空参数会报错，故使用[mockito-kotlin](https://github.com/nhaarman/mockito-kotlin)来实现，mockito-kotlin只是对Mockito的封装，本质上还是Mockito

```groovy
testImplementation 'com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0'
```



```kotlin
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
}
```

#### View层

```kotlin
//ILoginView
interface ILoginView {
    fun getUserName(): String

    fun getPassword(): String

    fun showToast(msg: String)
}
//LoginActivity
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
//PresenterManager
object PresenterManager {
    fun provideLoginPresenter(view: ILoginView): ILoginPresenter {
        return LoginPresenter(ModelManager.provideLoginModel(), view)
    }
}
//ModelManager
object ModelManager {
    fun provideLoginModel(): ILoginModel {
        return LoginModel()
    }
}
```



View层因为是Activity，是Android类，测起来复杂了点，可以使用[robolectric](https://github.com/robolectric/robolectric)来实现，robolectric把所有Android类都重新实现了一遍，可以在PC上执行，不需要在Android设备上。robolectric从4.0开始与androidx test完全兼容，可以用**AndroidJUnit4**来取代**RobolectricTestRunner**，另外robolectric从4.0开始也可以使用Espresso来执行一些UI相关操作，不需要像4.0以前全靠findViewById来了

```groovy
android {
  testOptions {
    unitTests {
      includeAndroidResources = true
    }
  }
}

dependencies {
    testImplementation 'org.robolectric:robolectric:4.3.1'
    testImplementation 'androidx.test.ext:junit:1.1.1'
    testImplementation 'androidx.test.espresso:espresso-core:3.2.0'
}
```

因为Activity不像其它类可以直接实例化，没有办法通过构造方法将presenter注入进去，所以使用`PresenterManager.provideLoginPresenter()`来注入，这个方法是静态的，用mockito-kotlin没办法mock静态方法，所以选用了一个专用于kotlin的库[mockk](https://github.com/mockk/mockk)，这个库实现了mockito所有功能并且实现了它没有的mock静态方法等，这个库完全可以取代mockito。但是它有一个缺点就是执行起来太慢，mockito是毫秒级的，这个可能是几十毫秒级的。所以一般两个库同时用，在测试与Android无关的类的时候用mockito，用robolectric时用mockk，因为本来robolectric就超慢。

```groovy
testImplementation "io.mockk:mockk:1.10.0"
```

用robolectric来测试Activity可以看成是把原本应该是仪器化测试的转化成了本地测试，这里应当把它当成是仪器化测试，可以叫它（伪仪器化测试）前面说到**单元测试的要遵守的一个原则，仅实例化当前测试类的对象，其它依赖类都mock掉**在这里就不适用了。伪仪器化测试不mock逻辑相关的presenter只mock网络请求，数据库请求等依赖设备的操作

```kotlin
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
```

