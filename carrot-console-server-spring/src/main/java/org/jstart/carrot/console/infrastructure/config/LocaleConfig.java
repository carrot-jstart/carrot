package org.jstart.carrot.console.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class LocaleConfig {
    private final I18nProperties i18nProperties;

    @Bean
    public LocaleResolver localeResolver() {
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                Locale locale = resolveFromTag(request.getHeader("X-Language"));
                if (locale != null) {
                    return locale;
                }
                locale = resolveFromTag(request.getParameter("lang"));
                if (locale != null) {
                    return locale;
                }
                String acceptLanguage = request.getHeader("Accept-Language");
                if (StringUtils.hasText(acceptLanguage) && request.getLocale() != null) {
                    return request.getLocale();
                }
                return i18nProperties.getDefaultLocale();
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                throw new UnsupportedOperationException("setLocale is not supported");
            }
        };
    }

    private Locale resolveFromTag(String languageTag) {
        if (!StringUtils.hasText(languageTag)) {
            return null;
        }
        Locale locale = i18nProperties.toLocale(languageTag);
        if (!StringUtils.hasText(locale.getLanguage())) {
            return null;
        }
        return locale;
    }
}
