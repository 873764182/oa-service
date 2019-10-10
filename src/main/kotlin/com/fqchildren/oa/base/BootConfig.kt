package com.fqchildren.oa.base

import com.fqchildren.oa.filter.IndexFilter
import com.fqchildren.oa.filter.NameFilter
import com.fqchildren.oa.filter.VerifyFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class BootConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var indexFilter: IndexFilter

    @Autowired
    private lateinit var nameFilter: NameFilter

    @Autowired
    private lateinit var verifyFilter: VerifyFilter

    @Bean
    fun regIndexFilter(): FilterRegistrationBean<IndexFilter> {
        val bean = FilterRegistrationBean<IndexFilter>()
        bean.filter = indexFilter
        bean.order = 1
        bean.addUrlPatterns("/*")
        return bean
    }

    @Bean
    fun regNameFilter(): FilterRegistrationBean<NameFilter> {
        val bean = FilterRegistrationBean<NameFilter>()
        bean.filter = nameFilter
        bean.order = 5
        bean.addUrlPatterns("/*")
        return bean
    }

    @Bean
    fun regVerifyFilter(): FilterRegistrationBean<VerifyFilter> {
        val bean = FilterRegistrationBean<VerifyFilter>()
        bean.filter = verifyFilter
        bean.order = 10
        bean.addUrlPatterns("/*")
        return bean
    }
}
