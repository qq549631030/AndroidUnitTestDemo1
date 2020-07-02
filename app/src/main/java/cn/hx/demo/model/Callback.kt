package cn.hx.demo.model

import java.lang.Exception

interface Callback {

    fun onSuccess(result: String)

    fun onError(e: Exception)
}