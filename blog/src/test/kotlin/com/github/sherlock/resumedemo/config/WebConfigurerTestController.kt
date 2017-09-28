package com.github.sherlock.resumedemo.config

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by TangBin on 2017/9/28.
 */

@RestController
class WebConfigurerTestController {

    @GetMapping("/api/test-cors")
    fun testCorsOnApiPath() {
    }

    @GetMapping("/test/test-cors")
    fun testCorsOnOtherPath() {
    }
}
