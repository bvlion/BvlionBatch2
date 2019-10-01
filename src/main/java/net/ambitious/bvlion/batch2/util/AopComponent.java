package net.ambitious.bvlion.batch2.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.web.exception.NotFoundException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

	@Before("execution(* net.ambitious.bvlion.batch2.web.controller.check..*.*(..))")
	public void controllerBefore() {
		var request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		var userAgent = request.getHeader("User-Agent");
		if (!appParams.getAllowUserAgent().equals(userAgent)) {
		    throw new NotFoundException();
		}
	}
}
