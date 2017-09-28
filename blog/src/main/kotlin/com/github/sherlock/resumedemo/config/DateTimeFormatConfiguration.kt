package com.github.sherlock.resumedemo.config

import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * Created by TangBin on 2017/9/28.
 */
@Configuration
class DateTimeFormatConfiguration : WebMvcConfigurerAdapter() {

    override fun addFormatters(registry: FormatterRegistry?) {
        val registrar = DateTimeFormatterRegistrar()
        registrar.setUseIsoFormat(true)
        registrar.registerFormatters(registry!!)
    }
}
