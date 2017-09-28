package com.github.sherlock.resumedemo.web.rest

import com.github.sherlock.resumedemo.service.SocialService
import org.slf4j.LoggerFactory
import org.springframework.social.connect.web.ProviderSignInUtils
import org.springframework.social.support.URIBuilder
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.view.RedirectView

/**
 * Created by TangBin on 2017/9/28.
 */
@RestController
@RequestMapping("/social")
class SocialController
constructor(
    private val socialService: SocialService,
    private val providerSignInUtils: ProviderSignInUtils
) {
    private val log = LoggerFactory.getLogger(SocialController::class.java)

    @GetMapping("/signup")
    fun signUp(webRequest: WebRequest, @CookieValue(name = "NG_TRANSLATE_LANG_KEY", required = false, defaultValue = "\"en\"") langKey: String): RedirectView {
        try {
            val connection = providerSignInUtils.getConnectionFromSession(webRequest)
            socialService.createSocialUser(connection, langKey.replace("\"", ""))
            return RedirectView(
                URIBuilder.fromUri("/#/social-register/" + connection.getKey().getProviderId())
                    .queryParam("success", "true")
                    .build().toString(), true
            )
        }
        catch (e: Exception) {
            log.error("Exception creating social user: ", e)
            return RedirectView(
                URIBuilder.fromUri("/#/social-register/no-provider")
                    .queryParam("success", "false")
                    .build().toString(), true
            )
        }
    }
}
