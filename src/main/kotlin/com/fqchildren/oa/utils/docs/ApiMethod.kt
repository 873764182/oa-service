package com.fqchildren.oa.utils.docs

@Target(AnnotationTarget.FUNCTION)
annotation class ApiMethod(
        val value: String = "",
        val depict: String = "",
        val params: Array<ApiParam> = [],
        val result: Array<ApiParam> = [],
        val example: String = ""
)
