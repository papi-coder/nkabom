package com.infobip.nkabom.approval.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class SesConfiguration {

    @Bean(destroyMethod = "close")
    SesV2Client sesV2Client(@Value("${aws.region}") String region) {
        return SesV2Client.builder()
                .region(Region.of(region))
                .build();
    }
}
