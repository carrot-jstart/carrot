package org.jstart.carrot.console.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.entity.vo.ExecutedResult;
import org.jstart.carrot.console.application.service.ResultCodeI18nService;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class ExecutedResultResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final ResultCodeI18nService resultCodeI18nService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ExecutedResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(body instanceof ExecutedResult<?> result)) {
            return body;
        }
        if (result.getMsgCode() == null || result.getMsgCode().isBlank()) {
            return body;
        }
        result.setMsg(resultCodeI18nService.getMessage(result.getMsgCode(), LocaleContextHolder.getLocale(),
                result.getMsg()));
        return result;
    }
}
