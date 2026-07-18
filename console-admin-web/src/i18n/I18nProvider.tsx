import React, { useEffect, useMemo, useState } from "react";
import dayjs from "dayjs";
import "dayjs/locale/en";
import "dayjs/locale/zh-cn";
import enUS from "antd/locale/en_US";
import zhCN from "antd/locale/zh_CN";
import type { Locale as AntdLocale } from "antd/es/locale";
import { getMessage, I18N_STORAGE_KEY, localeMessages, readPreferredLocale, type Locale } from "./messages";

type TranslateVars = Record<string, string | number>;

export type I18nContextValue = {
  locale: Locale;
  antLocale: AntdLocale;
  setLocale: (locale: Locale) => void;
  t: (key: keyof (typeof localeMessages)["zh-CN"] | string, vars?: TranslateVars) => string;
};

export const I18nContext = React.createContext<I18nContextValue | null>(null);

const dayjsLocaleMap: Record<Locale, string> = {
  "zh-CN": "zh-cn",
  "en-US": "en"
};

const antLocaleMap: Record<Locale, AntdLocale> = {
  "zh-CN": zhCN,
  "en-US": enUS
};

export function I18nProvider(props: React.PropsWithChildren) {
  const [locale, setLocaleState] = useState<Locale>(() => readPreferredLocale());

  useEffect(() => {
    dayjs.locale(dayjsLocaleMap[locale]);
    window.localStorage.setItem(I18N_STORAGE_KEY, locale);
  }, [locale]);

  const value = useMemo<I18nContextValue>(
    () => ({
      locale,
      antLocale: antLocaleMap[locale],
      setLocale: (nextLocale: Locale) => {
        setLocaleState(nextLocale);
      },
      t: (key, vars) => getMessage(locale, key, vars)
    }),
    [locale]
  );

  return <I18nContext.Provider value={value}>{props.children}</I18nContext.Provider>;
}
