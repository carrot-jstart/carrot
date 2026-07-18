import React, { useMemo, useState } from "react";
import { ConfigProvider, theme as antdTheme } from "antd";
import type { ThemeSettings } from "./themeTypes";
import { ThemeContext } from "./ThemeContext";
import { useI18n } from "../i18n/useI18n";

const STORAGE_KEY = "carrot_scheduling_theme";

function loadSettings(): ThemeSettings {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { mode: "light", primaryColor: "#1677ff" };
    }
    const parsed = JSON.parse(raw) as Partial<ThemeSettings>;
    return {
      mode: parsed.mode === "dark" ? "dark" : "light",
      primaryColor: typeof parsed.primaryColor === "string" ? parsed.primaryColor : "#1677ff",
    };
  } catch {
    return { mode: "light", primaryColor: "#1677ff" };
  }
}

function persistSettings(settings: ThemeSettings) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
}

export function ThemeProvider(props: React.PropsWithChildren) {
  const [settings, setSettings] = useState<ThemeSettings>(() => loadSettings());
  const { antLocale } = useI18n();

  const value = useMemo(
    () => ({
      settings,
      setMode: (mode: ThemeSettings["mode"]) => {
        const next = { ...settings, mode };
        setSettings(next);
        persistSettings(next);
      },
      setPrimaryColor: (primaryColor: string) => {
        const next = { ...settings, primaryColor };
        setSettings(next);
        persistSettings(next);
      },
    }),
    [settings],
  );

  const algorithm = settings.mode === "dark" ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm;

  return (
    <ThemeContext.Provider value={value}>
      <ConfigProvider
        locale={antLocale}
        theme={{
          algorithm,
          token: {
            colorPrimary: settings.primaryColor,
          },
        }}
      >
        {props.children}
      </ConfigProvider>
    </ThemeContext.Provider>
  );
}
