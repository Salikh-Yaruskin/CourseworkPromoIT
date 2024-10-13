package com.Announcements.Announcements;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.NewsRepository;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class NewsServiceTests {

	@Mock
	private NewsRepository newsRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private NewsService newsService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	private Users user;
	private News news;

	@BeforeEach
	void setUp() {
		// Создаем объект пользователя и новости для теста
		user = new Users();
		user.setId(1);
		user.setUsername("testuser");
		user.setLimitNews(2); // Лимит новостей на день

		news = new News();
		news.setName("Test News");
		news.setDescription("Test Description");
		news.setUser(user);

		// Мокируем SecurityContext и Authentication
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void testAddNews_Success() throws Exception {
		// Мокируем текущего пользователя
		when(userService.findByUsername("testuser")).thenReturn(user);

		// Мокируем количество новостей, опубликованных пользователем в течение дня
		when(newsRepository.countNewsByUserAndCreatedAtBetween(
				Mockito.eq(1),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).thenReturn(1); // 1 новость опубликована за день

		// Мокируем поведение проверки блокировки
		Mockito.doNothing().when(userService).checkBlocking(1);

		// Мокируем сохранение новости
		when(newsRepository.save(any(News.class))).thenReturn(news);

		// Добавляем новость
		News savedNews = newsService.addNews(news);

		// Проверяем, что новость была успешно добавлена
		assertNotNull(savedNews);
		assertEquals("Test News", savedNews.getName());
		verify(newsRepository, times(1)).save(any(News.class));
	}

	@Test
	void testAddNews_Fail_LimitExceeded() {
		// Мокируем текущего пользователя
		when(userService.findByUsername("testuser")).thenReturn(user);

		// Мокируем превышение лимита новостей за день
		when(newsRepository.countNewsByUserAndCreatedAtBetween(
				Mockito.eq(1),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).thenReturn(2); // 2 новости уже опубликованы, лимит = 2

		// Проверяем, что при добавлении новости превышается лимит
		Exception exception = assertThrows(Exception.class, () -> {
			newsService.addNews(news);
		});

		String expectedMessage = "Вы не можете публиковать больше 2 объявлений в день";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}
}
