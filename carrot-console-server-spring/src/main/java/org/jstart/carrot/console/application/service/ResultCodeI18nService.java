package org.jstart.carrot.console.application.service;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.infrastructure.config.I18nProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ResultCodeI18nService {
    private final MessageSource messageSource;
    private final I18nProperties i18nProperties;

    public String getMessage(EResultCode resultCode) {
        return getMessage(resultCode, currentLocaleOrDefault());
    }

    public String getMessage(EResultCode resultCode, Locale locale) {
        return getMessage(resultCode.getMessageKey(), locale, resultCode.getMessage());
    }

    public String getMessage(String messageKey, Locale locale, String defaultMessage) {
        Locale targetLocale = locale == null ? i18nProperties.getDefaultLocale() : locale;
        return messageSource.getMessage(messageKey, null, defaultMessage, targetLocale);
    }

    public Locale getDefaultLocale() {
        return i18nProperties.getDefaultLocale();
    }

    private Locale currentLocaleOrDefault() {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        if (localeContext != null && localeContext.getLocale() != null) {
            return localeContext.getLocale();
        }
        return i18nProperties.getDefaultLocale();
    }
}
