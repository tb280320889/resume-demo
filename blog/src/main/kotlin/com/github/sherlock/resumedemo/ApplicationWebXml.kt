package com.github.sherlock.resumedemo

import com.github.sherlock.resumedemo.config.util.DefaultProfileUtil
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

/**
 * Created by TangBin on 2017/9/27.
 * This is a helper Java class that provides an alternative to creating a web.xml.
 * This will be invoked only when the application is deployed to a servlet container like Tomcat, JBoss etc.
 */

class ApplicationWebXml : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        /**
         * set a default to use when no profile is configured.
         */
        DefaultProfileUtil.addDefaultProfile(application.application())
        return application.sources(BlogApp::class.java)
    }
}
