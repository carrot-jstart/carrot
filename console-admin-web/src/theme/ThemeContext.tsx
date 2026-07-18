import React from "react";
import type { ThemeSettings } from "./themeTypes";

export type ThemeContextValue = {
  settings: ThemeSettings;
  setMode: (mode: ThemeSettings["mode"]) => void;
  setPrimaryColor: (color: string) => void;
};

export const ThemeContext = React.createContext<ThemeContextValue | null>(null);

