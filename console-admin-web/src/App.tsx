import { Button, ColorPicker, Layout, Menu, Space, Switch, Tooltip, Typography } from "antd";
import type { MenuProps } from "antd";
import React, { useState } from "react";
import { Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { useTheme } from "./theme/useTheme";
import NamespacePage from "./pages/namespacePage";
import SchedulerPage from "./pages/schedulerPage";
import DiscoveryPage from "./pages/discoveryPage";
import ConfigPage from "./pages/configPage";
import { BgColorsOutlined, MoonOutlined, SunOutlined } from "@ant-design/icons";
import { useI18n } from "./i18n/useI18n";

const { Header, Sider, Content } = Layout;

type NavKey = "/namespace" | "/scheduler" | "/discovery" | "/config";

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const { settings, setMode, setPrimaryColor } = useTheme();
  const { locale, setLocale, t } = useI18n();
  const [colorOpen, setColorOpen] = useState(false);
  const navItems: MenuProps["items"] = [
    { key: "/namespace", label: t("app.nav.namespace") },
    { key: "/scheduler", label: t("app.nav.scheduler") },
    { key: "/discovery", label: t("app.nav.discovery") },
    { key: "/config", label: t("app.nav.config") }
  ];

  const selectedKey = ([
    "/namespace",
    "/scheduler",
    "/discovery",
    "/config",
  ] as string[]).includes(location.pathname)
    ? (location.pathname as NavKey)
    : ("/namespace" as NavKey);

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider width={200}>
        <div style={{ height: 56, display: "flex", alignItems: "center", justifyContent: "center", gap: 10, padding: "0 12px" }}>
          <svg width={86} height={26} viewBox="0 0 86 26" role="img" aria-label="Carrot" style={{ display: "block" }}>
            <defs>
              <linearGradient id="carrotWordGradient" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stopColor="#FFB86B" />
                <stop offset="55%" stopColor="#FF7A45" />
                <stop offset="100%" stopColor="#FF4D4F" />
              </linearGradient>
              <filter id="carrotWordShadow" x="-20%" y="-50%" width="140%" height="200%">
                <feDropShadow dx="0" dy="1" stdDeviation="1" floodColor="rgba(0,0,0,0.45)" />
              </filter>
            </defs>
            <text
              x="0"
              y="20"
              fill="url(#carrotWordGradient)"
              stroke="rgba(255,255,255,0.18)"
              strokeWidth="0.6"
              fontSize="20"
              fontWeight="700"
              fontFamily="'Segoe Script','Brush Script MT','Comic Sans MS',cursive"
              letterSpacing="0.8"
              filter="url(#carrotWordShadow)"
            >
              Carrot
            </text>
          </svg>
          <Typography.Text style={{ color: "rgba(255,255,255,0.92)", fontSize: 18, fontWeight: 800, lineHeight: 1 }}>
            1.0.0
          </Typography.Text>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={navItems}
          onClick={(e) => navigate(e.key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: "transparent", padding: "0 16px" }}>
          <div style={{ display: "flex", justifyContent: "flex-end", alignItems: "center", height: "100%" }}>
            <Space size={12}>
              <Tooltip title={`${t("app.language")}: ${locale === "en-US" ? t("app.language.en-US") : t("app.language.zh-CN")}`}>
                <Switch
                  checked={locale === "en-US"}
                  checkedChildren="EN"
                  unCheckedChildren="中"
                  onChange={(checked) => setLocale(checked ? "en-US" : "zh-CN")}
                  aria-label={t("app.language")}
                />
              </Tooltip>
              <Switch
                checked={settings.mode === "dark"}
                checkedChildren={<MoonOutlined />}
                unCheckedChildren={<SunOutlined />}
                onChange={(v) => setMode(v ? "dark" : "light")}
              />
              <ColorPicker
                value={settings.primaryColor}
                onChange={(_, hex) => setPrimaryColor(hex)}
                open={colorOpen}
                onOpenChange={setColorOpen}
              >
                <Tooltip title={colorOpen ? t("app.collapseThemeColor") : t("app.themeColor")}>
                  <Button type="text" icon={<BgColorsOutlined />} onClick={() => setColorOpen((v) => !v)} />
                </Tooltip>
              </ColorPicker>
            </Space>
          </div>
        </Header>
        <Content style={{ padding: 16 }}>
          <Routes>
            <Route path="/" element={<Navigate to="/namespace" replace />} />
            <Route path="/namespace" element={<NamespacePage />} />
            <Route path="/scheduler" element={<SchedulerPage />} />
            <Route path="/discovery" element={<DiscoveryPage />} />
            <Route path="/config" element={<ConfigPage />} />
            <Route path="*" element={<Navigate to="/namespace" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}
