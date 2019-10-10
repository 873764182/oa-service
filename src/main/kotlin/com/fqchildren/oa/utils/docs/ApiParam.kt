package com.fqchildren.oa.utils.docs

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ApiParam(
        val value: String = "",
        val depict: String = "",
        val required: Boolean = true,
        val type: ApiType = ApiType.String,
        val example: String = ""
)
