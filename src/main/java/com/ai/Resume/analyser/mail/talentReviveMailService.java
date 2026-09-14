package com.ai.Resume.analyser.mail;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.Configuration;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

/**
 * Reuses the existing Brevo transactional-email setup (same apiKey property
 * and ApiClient/TransactionalEmailsApi pattern as mailService, which already
 * sends OTP emails this way) instead of adding a second email transport.
 *
 * A previous draft added spring.mail.* SMTP credentials and used
 * JavaMailSender in two separate, unused-together classes - that's a
 * duplicate credential/transport for the same provider and has been removed.
 */
@Service
public class talentReviveMailService {

    @Value("${apiKey}")
    private String apiKey;

    @Value("${talentrevive.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    private final TemplateEngine templateEngine;

    public talentReviveMailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void sendOutreachEmail(String candidateName, String candidateEmail, String jobTitle, String token) throws ApiException {
        send("outreach-email", "New opportunity: " + jobTitle, candidateName, candidateEmail, jobTitle, token);
    }

    public void sendFollowUpEmail(String candidateName, String candidateEmail, String jobTitle, String token) throws ApiException {
        send("followup-email", "Following up: " + jobTitle, candidateName, candidateEmail, jobTitle, token);
    }

    private void send(String template, String subject, String candidateName, String candidateEmail, String jobTitle, String token) throws ApiException {
        Context context = new Context();
        context.setVariable("candidateName", candidateName != null ? candidateName : candidateEmail);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("interestedUrl", publicBaseUrl + "/api/public/respond?token=" + token + "&action=INTERESTED");
        context.setVariable("notInterestedUrl", publicBaseUrl + "/api/public/respond?token=" + token + "&action=NOT_INTERESTED");
        context.setVariable("optOutUrl", publicBaseUrl + "/api/public/respond?token=" + token + "&action=OPTED_OUT");

        String html = templateEngine.process(template, context);

        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setApiKey(apiKey);
        TransactionalEmailsApi api = new TransactionalEmailsApi(apiClient);

        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(new SendSmtpEmailSender().name("TalentRevive").email("utkarshsingh2k5@gmail.com"));
        email.setTo(Collections.singletonList(new SendSmtpEmailTo().name(candidateName).email(candidateEmail)));
        email.setSubject(subject);
        email.setHtmlContent(html);

        api.sendTransacEmail(email);
    }
}
