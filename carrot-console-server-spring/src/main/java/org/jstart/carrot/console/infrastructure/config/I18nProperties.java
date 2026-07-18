package org.jstart.carrot.console.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@ConfigurationProperties(prefix = "carrot.i18n")
public class I18nProperties {
    private String defaultLanguage = "zh-CN";

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public Locale getDefaultLocale() {
        return toLocale(defaultLanguage);
    }

    public Locale toLocale(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        Locale locale = Locale.forLanguageTag(languageTag.replace('_', '-'));
        if (locale.getLanguage() == null || locale.getLanguage().isBlank()) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return locale;
    }
}
