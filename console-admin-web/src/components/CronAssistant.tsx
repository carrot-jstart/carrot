import { Button, Card, Checkbox, Divider, Input, Space, Tag, Tooltip, Typography } from "antd";
import React, { useMemo, useState } from "react";
import dayjs from "dayjs";
import { CronExpressionParser } from "cron-parser";
import { ControlOutlined } from "@ant-design/icons";
import { useI18n } from "../i18n/useI18n";

type Props = {
  value?: string;
  onChange?: (value: string) => void;
};

type CheckboxValue = string | number;

function normalizeForPreview(expr: string) {
  const raw = (expr || "").trim();
  if (!raw) return raw;
  const parts = raw.split(/\s+/).filter(Boolean);
  if (parts.length === 5) {
    parts.unshift("0");
  }
  if (parts.length >= 6) {
    const previewParts = parts.slice(0, 6).map((p, idx) => {
      if (p === "?" && (idx === 3 || idx === 5)) return "*";
      return p;
    });
    return previewParts.join(" ");
  }
  return raw;
}

function formatList(values: number[], min: number, max: number) {
  const uniq = Array.from(new Set(values)).filter((v) => v >= min && v <= max).sort((a, b) => a - b);
  if (uniq.length === 0) return "*";
  return uniq.join(",");
}

function buildCron(parts: {
  sec: number[];
  min: number[];
  hour: number[];
  dom: number[];
  month: number[];
  dow: number[];
}) {
  const sec = formatList(parts.sec, 0, 59);
  const min = formatList(parts.min, 0, 59);
  const hour = formatList(parts.hour, 0, 23);
  const month = formatList(parts.month, 1, 12);

  const hasDom = parts.dom.length > 0;
  const hasDow = parts.dow.length > 0;

  const dom = hasDom ? formatList(parts.dom, 1, 31) : "*";
  const dow = hasDow ? formatList(parts.dow, 0, 7) : "?";

  if (hasDom && hasDow) {
    return `${sec} ${min} ${hour} ${dom} ${month} ?`;
  }

  if (!hasDom && hasDow) {
    return `${sec} ${min} ${hour} ? ${month} ${dow}`;
  }

  return `${sec} ${min} ${hour} ${dom} ${month} ?`;
}

function asNumberArray(values: CheckboxValue[]) {
  return values
    .map((v) => Number(v))
    .filter((v) => Number.isFinite(v)) as number[];
}

function nextThree(expr: string) {
  const normalized = normalizeForPreview(expr);
  const interval = CronExpressionParser.parse(normalized, { currentDate: new Date() });
  const list: string[] = [];
  for (let i = 0; i < 3; i += 1) {
    const d = interval.next().toDate();
    list.push(dayjs(d).format("YYYY-MM-DD HH:mm:ss"));
  }
  return list;
}

const seconds = Array.from({ length: 60 }).map((_, i) => ({ label: String(i).padStart(2, "0"), value: i }));
const minutes = seconds;
const hours = Array.from({ length: 24 }).map((_, i) => ({ label: String(i).padStart(2, "0"), value: i }));
const days = Array.from({ length: 31 }).map((_, i) => ({ label: String(i + 1), value: i + 1 }));
const months = Array.from({ length: 12 }).map((_, i) => ({ label: String(i + 1), value: i + 1 }));

export default function CronAssistant(props: Props) {
  const { t } = useI18n();
  const [generatorOpen, setGeneratorOpen] = useState(false);
  const [pickedSec, setPickedSec] = useState<number[]>([]);
  const [pickedMin, setPickedMin] = useState<number[]>([]);
  const [pickedHour, setPickedHour] = useState<number[]>([]);
  const [pickedDom, setPickedDom] = useState<number[]>([]);
  const [pickedMonth, setPickedMonth] = useState<number[]>([]);
  const [pickedDow, setPickedDow] = useState<number[]>([]);

  const value = props.value ?? "";

  const preview = useMemo(() => {
    if (!value.trim()) return { ok: false as const, list: [] as string[], error: "" };
    try {
      const list = nextThree(value);
      return { ok: true as const, list, error: "" };
    } catch (e) {
      return { ok: false as const, list: [] as string[], error: e instanceof Error ? e.message : t("cron.invalidExpression") };
    }
  }, [t, value]);

  const weeks = useMemo(
    () => [
      { label: t("cron.week.0"), value: 0 },
      { label: t("cron.week.1"), value: 1 },
      { label: t("cron.week.2"), value: 2 },
      { label: t("cron.week.3"), value: 3 },
      { label: t("cron.week.4"), value: 4 },
      { label: t("cron.week.5"), value: 5 },
      { label: t("cron.week.6"), value: 6 },
      { label: t("cron.week.7"), value: 7 },
    ],
    [t],
  );

  const generated = useMemo(() => {
    return buildCron({
      sec: pickedSec,
      min: pickedMin,
      hour: pickedHour,
      dom: pickedDom,
      month: pickedMonth,
      dow: pickedDow,
    });
  }, [pickedDom, pickedDow, pickedHour, pickedMin, pickedMonth, pickedSec]);

  const hasDom = pickedDom.length > 0;
  const hasDow = pickedDow.length > 0;

  return (
    <Space direction="vertical" size={10} style={{ width: "100%" }}>
      <Input
        value={value}
        onChange={(e) => props.onChange?.(e.target.value)}
        placeholder={t("cron.placeholder")}
        suffix={
          <Tooltip title={generatorOpen ? t("cron.closeGenerator") : t("cron.openGenerator")}>
            <Button
              type="text"
              size="small"
              icon={<ControlOutlined />}
              onClick={() => setGeneratorOpen((v) => !v)}
            />
          </Tooltip>
        }
      />
      <Space wrap>
        <Typography.Text type="secondary">{t("cron.recentThree")}</Typography.Text>
        {preview.ok ? (
          preview.list.map((t) => <Tag key={t}>{t}</Tag>)
        ) : (
          <Typography.Text type="danger">{preview.error || t("cron.invalidExpression")}</Typography.Text>
        )}
        <Typography.Text type="secondary">{t("cron.previewTip")}</Typography.Text>
      </Space>

      {generatorOpen ? (
        <Card size="small" title={t("cron.generatorTitle")}>
          <Space direction="vertical" size={10} style={{ width: "100%" }}>
            <Typography.Text type="secondary">{t("cron.generatorHint")}</Typography.Text>
            <Divider style={{ margin: "8px 0" }} />

            <Space wrap>
              <Typography.Text>{t("cron.second")}</Typography.Text>
              <Typography.Link onClick={() => setPickedSec([])}>{t("cron.clear")}</Typography.Link>
            </Space>
            <div
              style={{
                maxHeight: 160,
                overflow: "auto",
                padding: 8,
                border: "1px solid rgba(5,5,5,0.06)",
                borderRadius: 6,
              }}
            >
              <Checkbox.Group options={seconds} value={pickedSec} onChange={(v) => setPickedSec(asNumberArray(v as any))} />
            </div>

            <Space wrap>
              <Typography.Text>{t("cron.minute")}</Typography.Text>
              <Typography.Link onClick={() => setPickedMin([])}>{t("cron.clear")}</Typography.Link>
            </Space>
            <div
              style={{
                maxHeight: 160,
                overflow: "auto",
                padding: 8,
                border: "1px solid rgba(5,5,5,0.06)",
                borderRadius: 6,
              }}
            >
              <Checkbox.Group options={minutes} value={pickedMin} onChange={(v) => setPickedMin(asNumberArray(v as any))} />
            </div>

            <Space wrap>
              <Typography.Text>{t("cron.hour")}</Typography.Text>
              <Typography.Link onClick={() => setPickedHour([])}>{t("cron.clear")}</Typography.Link>
            </Space>
            <div
              style={{
                maxHeight: 120,
                overflow: "auto",
                padding: 8,
                border: "1px solid rgba(5,5,5,0.06)",
                borderRadius: 6,
              }}
            >
              <Checkbox.Group options={hours} value={pickedHour} onChange={(v) => setPickedHour(asNumberArray(v as any))} />
            </div>

            <Space wrap>
              <Typography.Text>{t("cron.month")}</Typography.Text>
              <Typography.Link onClick={() => setPickedMonth([])}>{t("cron.clear")}</Typography.Link>
            </Space>
            <div
              style={{
                maxHeight: 120,
                overflow: "auto",
                padding: 8,
                border: "1px solid rgba(5,5,5,0.06)",
                borderRadius: 6,
              }}
            >
              <Checkbox.Group options={months} value={pickedMonth} onChange={(v) => setPickedMonth(asNumberArray(v as any))} />
            </div>

            <Space wrap>
              <Typography.Text>{t("cron.dayOfMonth")}</Typography.Text>
              <Typography.Link onClick={() => setPickedDom([])} disabled={hasDow}>
                {t("cron.clear")}
              </Typography.Link>
            </Space>
            <div
              style={{
                maxHeight: 160,
                overflow: "auto",
                padding: 8,
                border: "1px solid rgba(5,5,5,0.06)",
                borderRadius: 6,
              }}
            >
              <Checkbox.Group
                options={days}
                value={pickedDom}
                onChange={(v) => setPickedDom(asNumberArray(v as any))}
                disabled={hasDow}
              />
            </div>

            <Space wrap>
              <Typography.Text>{t("cron.dayOfWeek")}</Typography.Text>
              <Typography.Link onClick={() => setPickedDow([])} disabled={hasDom}>
                {t("cron.clear")}
              </Typography.Link>
            </Space>
            <div
              style={{
                maxHeight: 120,
                overflow: "auto",
                padding: 8,
                border: "1px solid rgba(5,5,5,0.06)",
                borderRadius: 6,
              }}
            >
              <Checkbox.Group
                options={weeks}
                value={pickedDow}
                onChange={(v) => setPickedDow(asNumberArray(v as any))}
                disabled={hasDom}
              />
            </div>
            <Divider style={{ margin: "8px 0" }} />

            <Space wrap>
              <Typography.Text type="secondary">{t("cron.generated")}</Typography.Text>
              <Tag>{generated}</Tag>
              <Typography.Link
                onClick={() => {
                  props.onChange?.(generated);
                  setGeneratorOpen(false);
                }}
              >
                {t("cron.fill")}
              </Typography.Link>
            </Space>
          </Space>
        </Card>
      ) : null}
    </Space>
  );
}
