import type { ExecutedResult } from "./types";
import { getMessage, readPreferredLocale } from "../i18n/messages";

const DEFAULT_BASE = "/api";

function getBaseUrl() {
  return DEFAULT_BASE;
}

function getRequestLocale() {
  try {
    const value = window.localStorage.getItem("carrot_console_locale");
    return value === "en-US" ? "en-US" : "zh-CN";
  } catch {
    return "zh-CN";
  }
}

export async function requestJson<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${getBaseUrl()}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      "Accept-Language": getRequestLocale(),
      "X-Language": getRequestLocale(),
      ...(init?.headers ?? {}),
    },
  });

  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }

  const body = (await res.json()) as ExecutedResult<T>;
  if (body.code !== 200) {
    throw new Error(body.msg || getMessage(readPreferredLocale(), "error.requestFailed"));
  }
  return body.data;
}

export async function postJson<T>(path: string, payload?: unknown): Promise<T> {
  return requestJson<T>(path, {
    method: "POST",
    body: payload === undefined ? undefined : JSON.stringify(payload),
  });
}

export async function getJson<T>(path: string): Promise<T> {
  return requestJson<T>(path, { method: "GET" });
}
