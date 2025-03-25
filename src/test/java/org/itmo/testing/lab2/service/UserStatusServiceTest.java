package org.itmo.testing.lab2.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeAll
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @ParameterizedTest
    @CsvSource({
        "1, -10, Inactive",
        "2, 0, Inactive",
        "3, 50, Inactive",
        "4, 100, Active",
        "5, 150, Highly active"
    })
    public void testGetUserStatus_Active(int n, long activityTime, String expectedStatus) {
        // Настроим поведение mock-объекта
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(activityTime);

        String status = userStatusService.getUserStatus("user123");

        assertEquals(expectedStatus, status);

        verify(userAnalyticsService, times(n)).getTotalActivityTime("user123");
    }

    @Test
    public void getUserLastSessionDate() {
        LocalDateTime login = LocalDateTime.now().minusHours(1);
        LocalDateTime logout = LocalDateTime.now();
        List<UserAnalyticsService.Session> sessions =
                List.of(
                        new UserAnalyticsService.Session(login.plusDays(1), logout.plusDays(1)),
                        new UserAnalyticsService.Session(login.plusDays(10), logout.plusDays(10)),
                        new UserAnalyticsService.Session(login, logout));

        when(userAnalyticsService.getUserSessions("user123")).thenReturn(sessions);

        Optional<String> lastSession = userStatusService.getUserLastSessionDate("user123");

        assertTrue(lastSession.isPresent());
        assertEquals(logout.toLocalDate().toString(), lastSession.get());

        verify(userAnalyticsService, times(2)).getUserSessions("user123");
    }

    @Test
    public void getUserLastSessionDateNoSessions() {
        when(userAnalyticsService.getUserSessions("user123")).thenReturn(List.of());

        NoSuchElementException exc =
                assertThrows(
                        NoSuchElementException.class,
                        () -> userStatusService.getUserLastSessionDate("user123"));
        assertNull(exc.getMessage());
    }

    @Disabled
    public void getUserLastSessionDateNotCorrectOrder() {
        LocalDateTime login = LocalDateTime.now().minusHours(1);
        LocalDateTime logout = LocalDateTime.now();
        List<UserAnalyticsService.Session> sessions =
                List.of(
                        new UserAnalyticsService.Session(login, logout),
                        new UserAnalyticsService.Session(login.plusDays(1), logout.plusDays(1)),
                        new UserAnalyticsService.Session(login.plusDays(10), logout.plusDays(10)));

        when(userAnalyticsService.getUserSessions("user123")).thenReturn(sessions);

        Optional<String> lastSession = userStatusService.getUserLastSessionDate("user123");

        assertTrue(lastSession.isPresent());
        assertEquals(logout.toLocalDate().toString(), lastSession.get());

        verify(userAnalyticsService).getUserSessions("user123");
    }
}
