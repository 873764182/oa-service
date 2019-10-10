package com.fqchildren.oa.core

open class BaseApis {

    fun show(obj: Any, result: String?) {
        val name = obj.javaClass.name
        val method = Thread.currentThread().stackTrace[2].methodName
        println("$name -> $method：\n$result")
    }

}
