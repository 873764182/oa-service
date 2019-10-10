package com.fqchildren.oa.utils.permission

@Target(AnnotationTarget.FUNCTION)
annotation class Permission(val value: String = "")
