package net.ambitious.bvlion.batch2.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ProjectControllerAdvice {

    @NonNull
    private final AppParams appParams;

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    @ResponseBody
    public Map<String, String> handleError(Exception exception) {
        final String message = "ControllerでExceptionが発生したようです。";

        if (!(exception instanceof HttpRequestMethodNotSupportedException)
                && !(exception instanceof HttpMediaTypeNotSupportedException)) {
            AccessUtil.exceptionPost(message, log, exception, appParams);
        }

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", exception.getMessage());

        return errorMap;
    }
}