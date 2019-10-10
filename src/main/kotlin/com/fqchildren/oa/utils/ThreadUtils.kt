package com.fqchildren.oa.utils

import java.util.concurrent.Executors

object ThreadUtils {

    private val executor = Executors.newFixedThreadPool(10)

    /**
     * 延迟执行
     */
    fun delayMethod(async: AsyncInterface, time: Long = 1000) {
        executor.run {
            Thread.sleep(time)
            async.run()
        }
    }

}

interface AsyncInterface {
    fun run()
}
