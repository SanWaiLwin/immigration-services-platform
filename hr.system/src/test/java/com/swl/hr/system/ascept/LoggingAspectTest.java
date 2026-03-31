package com.swl.hr.system.ascept;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingAspectTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        logger.detachAndStopAllAppenders();
    }

    @Test
    @DisplayName("logAround logs start and end with status when response is present")
    void logAround_success_logsStartAndEnd_withStatus() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/job_data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        ServletRequestAttributes attrs = new ServletRequestAttributes(request, response);
        RequestContextHolder.setRequestAttributes(attrs);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.toShortString()).thenReturn("JobDataController.getJobData(..)");
        when(pjp.proceed()).thenReturn("OK");

        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        LoggingAspect aspect = new LoggingAspect();

        Object result = aspect.logAround(pjp);

        assertEquals("OK", result);
        assertFalse(listAppender.list.isEmpty(), "No logs captured");

        String startMsg = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(m -> m.startsWith("Start API call:"))
                .findFirst()
                .orElse(null);
        assertNotNull(startMsg);
        assertTrue(startMsg.contains("POST"));
        assertTrue(startMsg.contains("/api/job_data"));
        assertTrue(startMsg.contains("JobDataController.getJobData(..)"));

        String endMsg = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(m -> m.startsWith("End API call:"))
                .findFirst()
                .orElse(null);
        assertNotNull(endMsg);
        assertTrue(endMsg.contains("status=200"), "End log should include response status");
        assertTrue(endMsg.contains("duration="));
    }

    @Test
    @DisplayName("logAround logs error with exception when proceed throws")
    void logAround_exception_logsError() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.toShortString()).thenReturn("SomeController.someMethod(..)");
        when(pjp.proceed()).thenThrow(new RuntimeException("boom"));

        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        LoggingAspect aspect = new LoggingAspect();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.logAround(pjp));
        assertEquals("boom", ex.getMessage());

        String startMsg = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(m -> m.startsWith("Start API call:"))
                .findFirst()
                .orElse(null);
        assertNotNull(startMsg);
        assertTrue(startMsg.contains("GET"));
        assertTrue(startMsg.contains("/test"));
        assertTrue(startMsg.contains("SomeController.someMethod(..)"));

        String errorMsg = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(m -> m.startsWith("API call failed:"))
                .findFirst()
                .orElse(null);
        assertNotNull(errorMsg);
        assertTrue(errorMsg.contains("error=boom"));
        assertTrue(errorMsg.contains("duration="));
    }
}