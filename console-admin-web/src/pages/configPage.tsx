import { DeleteOutlined, EditOutlined, EyeOutlined } from "@ant-design/icons";
import { Button, Card, Empty, Form, Input, Modal, Popconfirm, Radio, Space, Table, Tabs, Tooltip, Typography, message, theme } from "antd";
import type { ColumnsType, TablePaginationConfig } from "antd/es/table";
import dayjs from "dayjs";
import React, { useEffect, useMemo, useState } from "react";
import Editor from "@monaco-editor/react";
import { namespaceApi } from "../api/namespace";
import type { ConfigItemSimpleVO, ConfigItemVO, NamespaceVO } from "../api/types";
import { configApi } from "../api/config";
import { useTheme } from "../theme/useTheme";
import { useI18n } from "../i18n/useI18n";

type UnitFilterForm = {
    likeDataId?: string;
    likeGroup?: string;
};

type UnitFilters = {
    eqNamespace: string;
    likeGroup?: string;
    likeDataId?: string;
};

const resolveEditorLanguage = (contentType?: string) => {
    const t = (contentType || "").trim().toLowerCase();
    if (!t) return "plaintext";
    if (t === "yaml" || t === "yml") return "yaml";
    if (t === "json") return "json";
    if (t === "xml") return "xml";
    if (t === "markdown" || t === "md") return "markdown";
    if (t === "html") return "html";
    if (t === "css") return "css";
    if (t === "javascript" || t === "js") return "javascript";
    if (t === "typescript" || t === "ts") return "typescript";
    if (t === "properties" || t === "prop") return "properties";
    if (t === "ini") return "ini";
    if (t === "sql") return "sql";
    if (t === "shell" || t === "bash" || t === "sh") return "shell";
    if (t === "text" || t === "txt" || t === "plain") return "plaintext";
    return "plaintext";
};


export default function ConfigPage() {
    const [searchForm] = Form.useForm<UnitFilterForm>();
    const [configForm] = Form.useForm<ConfigItemVO>();
    const { token } = theme.useToken();
    const { settings } = useTheme();
    const { t } = useI18n();
    const [namespaces, setNamespaces] = useState<NamespaceVO[]>([]);
    const [filters, setFilters] = useState<UnitFilters>({ eqNamespace: "" });
    const [unitsLoading, setUnitsLoading] = useState(false);
    const [units, setServuces] = useState<ConfigItemSimpleVO[]>([]);
    const [unitsTotal, setUnitsTotal] = useState(0);
    const [unitsPageIndex, setUnitsPageIndex] = useState(1);
    const [unitsPageSize, setUnitsPageSize] = useState(10);
    const [configModalOpen, setConfigModalOpen] = useState(false);
    const [configLoading, setConfigLoading] = useState(false);
    const [config, setConfig] = useState<ConfigItemVO>();
    const [addConfig, setAddConfig] = useState(false);
    const [viewConfig, setViewConfig] = useState(false);
    const [modifyConfig, setModifyConfig] = useState(false);
    const [configDraftContent, setConfigDraftContent] = useState("");
    const [lastLoadedConfigId, setLastLoadedConfigId] = useState<string | undefined>(undefined);
    const [viewportWidth, setViewportWidth] = useState<number>(() => (typeof window === "undefined" ? 1200 : window.innerWidth));
    useEffect(() => {
        const onResize = () => setViewportWidth(window.innerWidth);
        window.addEventListener("resize", onResize);
        return () => window.removeEventListener("resize", onResize);
    }, []);

    const logModalWidth = useMemo(() => {
        const w = Math.max(900, viewportWidth - 64);
        return Math.min(1400, w);
    }, [viewportWidth]);

    const editorTheme = settings.mode === "dark" ? "vs-dark" : "vs";



    const namespaceTabs = useMemo(
        () =>
            namespaces.map((item) => ({
                key: item.id,
                label: item.name || item.id,
            })),
        [namespaces],
    );

    const loadUnits = async (nextFilters: UnitFilters, pageIndex = unitsPageIndex, pageSize = unitsPageSize) => {
        if (!nextFilters.eqNamespace) {
            setServuces([]);
            setUnitsTotal(0);
            return;
        }
        setUnitsLoading(true);
        try {
            const res = await configApi.pageConfigItemSimple({
                pageIndex,
                pageSize,
                eqNamespace: nextFilters.eqNamespace,
                // 后端当前将执行器与组字段映射反了，这里做兼容转换。
                likeGroup: nextFilters.likeGroup || undefined,
                likeDataId: nextFilters.likeDataId || undefined,
            });
            setServuces(res.data ?? []);
            setUnitsTotal(Number(res.total ?? 0));
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("config.loadUnitsFailed"));
        } finally {
            setUnitsLoading(false);
        }
    };

    const applyNamespace = async (namespaceId: string) => {
        const nextFilters: UnitFilters = { eqNamespace: namespaceId, likeGroup: undefined, likeDataId: undefined };
        searchForm.setFieldsValue({ likeGroup: undefined, likeDataId: undefined });
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        setUnitsPageSize(10);
        await loadUnits(nextFilters, 1, 10);
    };

    const refreshNamespaces = async () => {
        try {
            const list = await namespaceApi.namespaceAll();
            const rows = list ?? [];
            setNamespaces(rows);
            const nextNamespaceId = filters.eqNamespace || rows[0]?.id || "";
            if (nextNamespaceId) {
                await applyNamespace(nextNamespaceId);
            } else {
                setServuces([]);
                setUnitsTotal(0);
            }
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("config.loadNamespacesFailed"));
        }
    };

    useEffect(() => {
        void refreshNamespaces();
    }, []);

    const handleSearch = async () => {
        const values = searchForm.getFieldsValue();
        const nextFilters: UnitFilters = {
            eqNamespace: filters.eqNamespace,
            likeGroup: values.likeGroup || undefined,
            likeDataId: values.likeDataId || undefined,
        };
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        await loadUnits(nextFilters, 1, unitsPageSize);
    };

    const handleReset = async () => {
        const namespaceId = filters.eqNamespace;
        searchForm.setFieldsValue({
            likeDataId: undefined,
            likeGroup: undefined
        });
        const nextFilters: UnitFilters = { eqNamespace: namespaceId, likeGroup: undefined, likeDataId: undefined };
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        await loadUnits(nextFilters, 1, unitsPageSize);
    };

    const openConfigViewModal = async (row: ConfigItemSimpleVO) => {
        setViewConfig(true);
        setAddConfig(false);
        setModifyConfig(false);
        setConfigModalOpen(true);
        setConfigLoading(true);
        try {
            const res = await configApi.getById(row.id);
            setConfig(res);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("config.loadConfigFailed"));
        } finally {
            setConfigLoading(false);
        }
    };

    const openConfigModifyModal = async (row: ConfigItemSimpleVO) => {
        setViewConfig(false);
        setAddConfig(false);
        setModifyConfig(true);
        setConfigModalOpen(true);
        setConfigLoading(true);
        try {
            const res = await configApi.getById(row.id);
            setConfig(res);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("config.loadConfigFailed"));
        } finally {
            setConfigLoading(false);
        }
    };

    const openAddConfigModal = () => {
        setViewConfig(false);
        setAddConfig(true);
        setModifyConfig(false);
        setConfigModalOpen(true);
        setConfigLoading(true);
        setConfig(undefined);
        setConfigDraftContent("");
        setLastLoadedConfigId(undefined);
        configForm.resetFields();
    };

    useEffect(() => {
        if (!configModalOpen) return;
        if (!config?.id) return;
        if (config.id === lastLoadedConfigId) return;
        setConfigDraftContent(config.content ?? "");
        setLastLoadedConfigId(config.id);
    }, [configModalOpen, config?.id, config?.content, lastLoadedConfigId]);

    useEffect(() => {
        configForm.setFieldsValue({
            id: config?.id ?? "",
            groupName: config?.groupName ?? "DEFAULT_GROUP",
            dataId: config?.dataId ?? "",
            contentType: config?.contentType ?? "TEXT",
            content: config?.content ?? "",
        });
    }, [config, configForm]);

    const handleCloseConfigModal = () => {
        setViewConfig(false);
        setAddConfig(false);
        setModifyConfig(false);
        setConfigModalOpen(false);
        setConfig(undefined);
        setConfigDraftContent("");
        setLastLoadedConfigId(undefined);
        configForm.resetFields();
    };


    const deleteConfig = async (id: string) => {
        try {
            await configApi.delete(id);
            message.success(t("config.deleteSuccess"));
            void loadUnits(filters, unitsPageIndex, unitsPageSize);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("config.deleteFailed"));
        }
    };

    const serviceColumns: ColumnsType<ConfigItemSimpleVO> = [
        { title: t("config.list.dataId"), dataIndex: "dataId", key: "dataId", },
        {
            title: t("config.list.group"),
            dataIndex: "groupName",
            key: "groupName",
        },
        { title: t("config.list.type"), dataIndex: "contentType", key: "contentType", },
        {
            title: t("config.list.updatedAt"), dataIndex: "updateTime", key: "updateTime",
            render: (v) => dayjs(v).format("YYYY-MM-DD HH:mm:ss"),
        },
        {
            title: t("common.actions"),
            key: "actions",
            align: "center",
            render: (_, row) => (
                <Space wrap size="small" style={{ justifyContent: "center", width: "100%" }}>
                    <Tooltip title={t("config.viewConfig")}>
                        <Button
                            size="small"
                            type="text"
                            icon={<EyeOutlined />}
                            onClick={() => {
                                void openConfigViewModal(row);
                            }}
                        />
                    </Tooltip>
                    <Tooltip title={t("config.editConfig")}>
                        <Button
                            size="small"
                            type="text"
                            icon={<EditOutlined />}
                            onClick={() => {
                                void openConfigModifyModal(row);
                            }}
                        />
                    </Tooltip>
                    <Popconfirm
                        title={t("config.deleteConfirm")}
                        okText={t("common.confirm")}
                        cancelText={t("common.cancel")}
                        onConfirm={() => {
                            void deleteConfig(row.id);
                        }}
                    >
                        <Tooltip title={t("config.deleteConfig")}>
                            <Button
                                size="small"
                                type="text"
                                danger
                                icon={<DeleteOutlined />}
                            />
                        </Tooltip>
                    </Popconfirm>
                </Space>
            ),
        },
    ];


    const unitPagination: TablePaginationConfig = {
        current: unitsPageIndex,
        pageSize: unitsPageSize,
        total: unitsTotal,
        showSizeChanger: true,
        onChange: (page, pageSize) => {
            setUnitsPageIndex(page);
            setUnitsPageSize(pageSize ?? 10);
            void loadUnits(filters, page, pageSize ?? 10);
        },
    };


    const handleSubmit = async (values: ConfigItemVO) => {
        if (modifyConfig) {
            await configApi.modify({
                id: config?.id ?? "",
                content: values.content,
                contentType: values.contentType,
            });
        } else {
            await configApi.create({
                namespace: filters.eqNamespace ?? "",
                groupName: values.groupName,
                dataId: values.dataId,
                contentType: values.contentType,
                content: values.content,
            });
        }
        handleCloseConfigModal();
    };


    return (
        <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Card bordered={false} bodyStyle={{ paddingBottom: 8 }}>
                {namespaceTabs.length > 0 ? (
                    <Tabs
                        activeKey={filters.eqNamespace || undefined}
                        items={namespaceTabs}
                        onChange={(key) => void applyNamespace(key)}
                    />
                ) : (
                    <Empty description={t("common.noNamespace")} />
                )}
            </Card>

            <Card
                title={
                    <Space wrap>
                        <Typography.Text>
                            {t("common.currentNamespace")}
                            {(namespaces.find((item) => item.id === filters.eqNamespace)?.id || filters.eqNamespace) ?? "-"}
                        </Typography.Text>
                    </Space>
                }
            >
                <Form form={searchForm} layout="inline" onFinish={() => void handleSearch()}>
                    <Form.Item label={t("config.search.dataIdLabel")} name="dataId">
                        <Input allowClear placeholder={t("config.search.dataIdPlaceholder")} style={{ width: 240 }} disabled={!filters.eqNamespace} />
                    </Form.Item>
                    <Form.Item label={t("config.search.groupLabel")} name="group">
                        <Input allowClear placeholder={t("config.search.groupPlaceholder")} style={{ width: 240 }} disabled={!filters.eqNamespace} />
                    </Form.Item>
                    <Form.Item>
                        <Space>
                            <Button type="primary" htmlType="submit" disabled={!filters.eqNamespace}>
                                {t("common.search")}
                            </Button>
                            <Button onClick={() => void handleReset()} disabled={!filters.eqNamespace}>
                                {t("common.reset")}
                            </Button>
                            <Button onClick={() => void openAddConfigModal()} disabled={!filters.eqNamespace}>
                                {t("config.addConfig")}
                            </Button>
                        </Space>
                    </Form.Item>
                </Form>
            </Card>

            {!filters.eqNamespace ? (
                <Card bordered={false}>
                    <Empty description={t("common.selectNamespaceFirst")} />
                </Card>
            ) : (
                <Card>
                    <Table rowKey="id" loading={unitsLoading} dataSource={units} columns={serviceColumns} pagination={unitPagination} scroll={{ x: 1500 }} />
                </Card>
            )}
            <Modal
                title={viewConfig ? t("config.modal.viewTitle") : addConfig ? t("config.modal.addTitle") : t("config.modal.editTitle")}
                open={configModalOpen}
                width={logModalWidth}
                onCancel={handleCloseConfigModal}
                destroyOnHidden
                footer={<></>}>
                <Form form={configForm} name="configForm" wrapperCol={{ flex: 2 }} onFinish={handleSubmit}>
                    <Form.Item label={t("config.namespace")}>
                        <Typography.Text strong>{filters.eqNamespace || "-"}</Typography.Text>
                    </Form.Item>
                    <Form.Item label={t("config.group")} name="groupName">
                        <Input readOnly={viewConfig || modifyConfig} placeholder={t("config.groupPlaceholder")} />
                    </Form.Item>
                    <Form.Item label={t("config.dataId")} name="dataId">
                        <Input placeholder={t("config.search.dataIdPlaceholder")} readOnly={viewConfig || modifyConfig} />
                    </Form.Item>
                    {!addConfig && (
                        <Form.Item label={t("common.createTime")}>
                            <Typography.Text strong>{config?.createTime ? dayjs(config.createTime).format("YYYY-MM-DD HH:mm:ss") : "-"}</Typography.Text>
                        </Form.Item>
                    )}
                    {!addConfig && (
                        <Form.Item label={t("common.updateTime")}>
                            <Typography.Text strong>{config?.updateTime ? dayjs(config.updateTime).format("YYYY-MM-DD HH:mm:ss") : "-"}</Typography.Text>
                        </Form.Item>
                    )}
                    <Form.Item label={t("config.contentType")} name="contentType">
                        {!viewConfig && (
                            <Radio.Group
                                options={[
                                    { value: "TEXT", label: 'TEXT' },
                                    { value: "JSON", label: 'JSON' },
                                    { value: "YAML", label: 'YAML' },
                                    { value: "Properties", label: 'Properties' },
                                    { value: "XML", label: 'XML' },
                                ]}
                            />
                        )}
                        {
                            viewConfig && (
                                <Typography.Text strong>{config?.contentType || "-"}</Typography.Text>
                            )
                        }
                    </Form.Item>
                    {modifyConfig && (
                        <Space wrap style={{ marginBottom: 8 }}>
                            <Button
                                size="small"
                                onClick={() => setConfigDraftContent(config?.content ?? "")}
                                disabled={configDraftContent === (config?.content ?? "")}
                            >
                                {t("config.resetDraft")}
                            </Button>
                        </Space>
                    )}
                    <Form.Item label={t("config.contentLabel")} name="content">
                        <Editor
                            height="40vh"
                            theme={editorTheme}
                            language={resolveEditorLanguage(configForm.getFieldValue("contentType"))}
                            value={configDraftContent}
                            loading={configLoading ? t("common.loading") : undefined}
                            onChange={(value) => setConfigDraftContent(value ?? "")}
                            options={{
                                readOnly: viewConfig,
                                minimap: { enabled: false },
                                scrollBeyondLastLine: false,
                                wordWrap: "on",
                                fontSize: 13,
                                tabSize: 2,
                                automaticLayout: true,
                            }}
                        />
                    </Form.Item>
                    {(modifyConfig || addConfig) && (
                        <Form.Item>
                            <Button type="primary" htmlType="submit">{t("common.save")}</Button>
                        </Form.Item>
                    )}
                </Form>
            </Modal>
        </Space>
    );
}
