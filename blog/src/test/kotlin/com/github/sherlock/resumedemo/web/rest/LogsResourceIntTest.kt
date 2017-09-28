package com.github.sherlock.resumedemo.web.rest

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.LoggerContext
import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.web.rest.util.TestUtil
import com.github.sherlock.resumedemo.web.rest.vm.LoggerVM
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Created by TangBin on 2017/9/28.
 */

@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
class LogsResourceIntTest {

    private var restLogsMockMvc: MockMvc? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val logsResource = LogsResource()
        this.restLogsMockMvc = MockMvcBuilders
            .standaloneSetup(logsResource)
            .build()
    }

    @Test
    @Throws(Exception::class)
    fun getAllLogs() {
        restLogsMockMvc!!.perform(get("/management/logs"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    @Throws(Exception::class)
    fun changeLogs() {
        val logger = LoggerVM()
        logger.level = ("INFO")
        logger.name = ("ROOT")

        restLogsMockMvc!!.perform(
            put("/management/logs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(logger))
        )
            .andExpect(status().isNoContent())
    }

    @Test
    fun testLogstashAppender() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        assertThat(context.getLogger("ROOT").getAppender("ASYNC_LOGSTASH")).isInstanceOf(AsyncAppender::class.java)
    }
}
