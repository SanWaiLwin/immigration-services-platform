package com.swl.hr.system.ascept;

import org.aspectj.lang.annotation.Aspect;
// Remove old advice imports and use Around advice instead
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Define pointcut for all controller methods
    @Pointcut("execution(public * com.swl.hr.system.controller.*.*(..))")
    public void controllerMethods() {
        // Pointcut expression to match all methods in controllers
    }

    // Replace multiple advices with a single Around advice to log start and end with duration
    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        String uri = request != null ? request.getRequestURI() : "N/A";
        String httpMethod = request != null ? request.getMethod() : "N/A";
        String methodName = pjp.getSignature().toShortString();

        logger.info("Start API call: {} {} -> {}", httpMethod, uri, methodName);

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            HttpServletResponse response = attrs != null ? attrs.getResponse() : null;
            Integer status = response != null ? response.getStatus() : null;
            if (status != null) {
                logger.info("End API call: {} {} -> {} | status={} | duration={}ms", httpMethod, uri, methodName, status, duration);
            } else {
                logger.info("End API call: {} {} -> {} | duration={}ms", httpMethod, uri, methodName, duration);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            logger.error("API call failed: {} {} -> {} | duration={}ms | error={}", httpMethod, uri, methodName, duration, ex.getMessage(), ex);
            throw ex;
        }
    }
}
