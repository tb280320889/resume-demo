package com.github.sherlock.resumedemo.service

import com.github.sherlock.resumedemo.BlogApp
import com.github.sherlock.resumedemo.domain.User
import io.github.jhipster.config.JHipsterProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Matchers.any
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.MessageSource
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.junit4.SpringRunner
import org.thymeleaf.spring4.SpringTemplateEngine
import java.io.ByteArrayOutputStream
import javax.mail.Multipart
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Created by TangBin on 2017/9/27.
 */


@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(BlogApp::class))
class MailServiceIntTest {

    @Autowired
    private val jHipsterProperties: JHipsterProperties? = null

    @Autowired
    private val messageSource: MessageSource? = null

    @Autowired
    private val templateEngine: SpringTemplateEngine? = null

    @Spy
    private val javaMailSender: JavaMailSenderImpl? = null

    @Captor
    private val messageCaptor: ArgumentCaptor<*>? = null

    private var mailService: MailService? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        doNothing().`when`(javaMailSender)!!.send(any<MimeMessage>(MimeMessage::class.java))
        mailService = MailService(jHipsterProperties!!, javaMailSender!!, messageSource!!, templateEngine!!)
    }

    @Test
    @Throws(Exception::class)
    fun testSendEmail() {
        mailService!!.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(String::class.java)
        assertThat(message.content.toString()).isEqualTo("testContent")
        assertThat(message.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendHtmlEmail() {
        mailService!!.sendEmail("john.doe@example.com", "testSubject", "testContent", false, true)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(String::class.java)
        assertThat(message.content.toString()).isEqualTo("testContent")
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendMultipartEmail() {
        mailService!!.sendEmail("john.doe@example.com", "testSubject", "testContent", true, false)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        val mp = message.content as MimeMultipart
        val part = (mp.getBodyPart(0).content as MimeMultipart).getBodyPart(0) as MimeBodyPart
        val aos = ByteArrayOutputStream()
        part.writeTo(aos)
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(Multipart::class.java)
        assertThat(aos.toString()).isEqualTo("\r\ntestContent")
        assertThat(part.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendMultipartHtmlEmail() {
        mailService!!.sendEmail("john.doe@example.com", "testSubject", "testContent", true, true)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        val mp = message.content as MimeMultipart
        val part = (mp.getBodyPart(0).content as MimeMultipart).getBodyPart(0) as MimeBodyPart
        val aos = ByteArrayOutputStream()
        part.writeTo(aos)
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(Multipart::class.java)
        assertThat(aos.toString()).isEqualTo("\r\ntestContent")
        assertThat(part.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendEmailFromTemplate() {
        val user = User()
        user.login = ("john")
        user.email = ("john.doe@example.com")
        user.langKey = ("en")
        mailService!!.sendEmailFromTemplate(user, "testEmail", "email.test.title")
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        assertThat(message.subject).isEqualTo("test title")
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isEqualTo("<html>test title, http://127.0.0.1:8080, john</html>\n")
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendActivationEmail() {
        val user = User()
        user.langKey = ("en")
        user.login = ("john")
        user.email = ("john.doe@example.com")
        mailService!!.sendActivationEmail(user)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isNotEmpty()
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testCreationEmail() {
        val user = User()
        user.langKey = ("en")
        user.login = ("john")
        user.email = ("john.doe@example.com")
        mailService!!.sendCreationEmail(user)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isNotEmpty()
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendPasswordResetMail() {
        val user = User()
        user.langKey = ("en")
        user.login = ("john")
        user.email = ("john.doe@example.com")
        mailService!!.sendPasswordResetMail(user)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor!!.capture() as MimeMessage)
        val message = messageCaptor.value as MimeMessage
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isNotEmpty()
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendEmailWithException() {
        doThrow(MailSendException::class.java).`when`(javaMailSender)!!.send(any<MimeMessage>(MimeMessage::class.java))
        mailService!!.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false)
    }

}
