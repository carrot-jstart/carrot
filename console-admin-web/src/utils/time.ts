import dayjs from "dayjs";

export function formatTs(ts?: number) {
  if (!ts) return "-";
  return dayjs(ts).format("YYYY-MM-DD HH:mm:ss");
}

export function formatTsMs(ts?: number) {
  if (!ts) return "-";
  return dayjs(ts).format("YYYY-MM-DD HH:mm:ss.SSS");
}

export function durationMs(start?: number, end?: number) {
  if (!start || !end) return "-";
  const diff = end - start;
  if (diff < 0) return "-";
  if (diff < 1000) return `${diff}ms`;
  return `${(diff / 1000).toFixed(3)}s`;
}
