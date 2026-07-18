import { useContext } from "react";
import { I18nContext } from "./I18nProvider";

export function useI18n() {
  const ctx = useContext(I18nContext);
  if (!ctx) {
    throw new Error("I18nContext not found");
  }
  return ctx;
}
