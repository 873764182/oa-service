package com.fqchildren.oa

import com.fqchildren.oa.service.InitService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
class App : SpringBootServletInitializer(), ApplicationListener<ApplicationReadyEvent> {
    private val log = LoggerFactory.getLogger(App::class.java)

    @Autowired
    private lateinit var initService: InitService

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        initService.run(event)
        log.info("Spring boot 应用已启动完成 ${event.applicationContext.hashCode()}")
    }

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(App::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
