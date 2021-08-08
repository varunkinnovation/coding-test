package com.db.awmd.challenge;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.db.awmd.challenge.service.NotificationService;


@Configuration
public class NotificationServiceTestConfig {

	@Bean
	@Primary
	public NotificationService notificationService() {
		return Mockito.mock(NotificationService.class);
	}

}
