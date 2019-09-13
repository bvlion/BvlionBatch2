package net.ambitious.bvlion.batch2.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AopComponent {

	@NonNull
	private final AppParams appParams;

	@Around("execution(* net.ambitious.bvlion.batch2.web.controller.check..(..))")
	public Object controllerBefore(ProceedingJoinPoint pjp) throws Throwable {
		var request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		var userAgent = request.getHeader("User-Agent");
		if (!appParams.getAllowUserAgent().equals(userAgent)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return pjp.proceed();
	}
}
