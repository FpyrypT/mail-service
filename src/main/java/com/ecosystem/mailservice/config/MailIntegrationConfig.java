package com.ecosystem.mailservice.config;

import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.MessageHandlerChain;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.MessageHandler;

import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.Properties;

@Configuration
@EnableIntegration
public class MailIntegrationConfig {

    @Bean
    public MailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public MailSendingMessageHandler mailSendingMessageHandler(MailSender javaMailSender) {
        return new MailSendingMessageHandler(javaMailSender);
    }

    @Bean
    public Properties javaMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.imap.socketFactory.fallback", "false");
        properties.put("ail.store.protocol", "imaps");
        properties.put("mail.debug", "false");

        return properties;
    }

    @Bean
    public ImapMailReceiver receiver(Properties javaMailProperties) {
        ImapMailReceiver receiver = new ImapMailReceiver("imaps://yourlogin:yourpasswprd@imap.gmail.com:993/inbox");
        receiver.setShouldDeleteMessages(false);
        receiver.setShouldMarkMessagesAsRead(false);
        receiver.setJavaMailProperties(javaMailProperties);
        return receiver;
    }


    @Bean
    public DirectChannel emailMessageChannel(MessageHandlerChain messageHandlerChain) {
        DirectChannel directChannel = new DirectChannel();
        directChannel.subscribe(message -> {
            try {
                MimeMessageParser parser = new MimeMessageParser(new MimeMessage((MimeMessage) message.getPayload()));
                parser.parse();
                System.out.println(parser.getPlainContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
//        directChannel.subscribe(messageHandlerChain);
        return directChannel;
    }

    @Bean
    public ImapIdleChannelAdapter imapIdleChannelAdapter(ImapMailReceiver receiver, DirectChannel emailMessageChannel) {
        ImapIdleChannelAdapter adapter = new ImapIdleChannelAdapter(receiver);
        adapter.setAutoStartup(true);
        adapter.setOutputChannel(emailMessageChannel);
        return adapter;
    }

    @Bean
    public MessageHandlerChain messageHandlerChain() {
        MessageHandlerChain chain = new MessageHandlerChain();

        MessageHandler handler = message -> {
            System.out.println(message.getPayload().toString());
        };

        chain.setHandlers(Collections.singletonList(handler));

        return chain;
    }

}
