import { Button, Card, Empty, Form, Input, Modal, Popconfirm, Select, Space, Switch, Table, Tabs, Tag, Tooltip, Typography, message, theme } from "antd";
import type { ColumnsType, TablePaginationConfig } from "antd/es/table";
import { CronExpressionParser } from "cron-parser";
import dayjs from "dayjs";
import React, { useEffect, useMemo, useState } from "react";
import { schedulingApi } from "../api/scheduling";
import { namespaceApi } from "../api/namespace";
import type { ExecutorNodeVO, JobRecordVO, JobUnitVO, NamespaceVO } from "../api/types";
import { durationMs, formatTs, formatTsMs } from "../utils/time";
import { useI18n } from "../i18n/useI18n";

type UnitFilterForm = {
    executorName?: string;
    groupName?: string;
    unitName?: string;
};

type UnitFilters = {
    namespaceId: string;
    executorName?: string;
    groupName?: string;
    unitName?: string;
};

type LogFilterForm = {
    code?: number;
};

type Translate = (key: string, vars?: Record<string, string | number>) => string;

function unitTypeLabel(type: number | undefined, t: Translate) {
    if (type === 1) return t("scheduler.fixedInterval");
    if (type === 2) return t("scheduler.cron");
    return t("common.none");
}

function maskSecret(secret?: string) {
    if (!secret) return "-";
    if (secret.length <= 6) return "***";
    return `${secret.slice(0, 3)}***${secret.slice(-3)}`;
}

function normalizeCronExpression(expr: string) {
    const raw = (expr || "").trim();
    if (!raw) return raw;
    const parts = raw.split(/\s+/).filter(Boolean);
    if (parts.length === 5) {
        parts.unshift("0");
    }
    if (parts.length >= 6) {
        return parts
            .slice(0, 6)
            .map((part, index) => {
                if (part === "?" && (index === 3 || index === 5)) return "*";
                return part;
            })
            .join(" ");
    }
    return raw;
}

function getNextThreeCronTimes(expr: string | undefined, t: Translate) {
    const value = (expr || "").trim();
    if (!value) return { ok: true as const, list: [] as string[], error: "" };
    try {
        const interval = CronExpressionParser.parse(normalizeCronExpression(value), { currentDate: new Date() });
        const list: string[] = [];
        for (let i = 0; i < 3; i += 1) {
            list.push(dayjs(interval.next().toDate()).format("YYYY-MM-DD HH:mm:ss"));
        }
        return { ok: true as const, list, error: "" };
    } catch (e) {
        return { ok: false as const, list: [] as string[], error: e instanceof Error ? e.message : t("cron.invalidExpression") };
    }
}

function codeTag(code: number | undefined, t: Translate) {
    if (code === undefined || code === null) return <Tag>{t("common.unknown")}</Tag>;
    if (code === 0) return <Tag color="default">{t("common.notStarted")}</Tag>;
    if (code === 200) return <Tag color="green">{t("common.success")}</Tag>;
    if (code === 400) return <Tag color="gold">{t("common.notFoundExecutor")}</Tag>;
    if (code === 403) return <Tag color="orange">{t("common.rejectExecute")}</Tag>;
    if (code === 500) return <Tag color="red">{t("common.failed")}</Tag>;
    return <Tag>{code}</Tag>;
}

function shortText(value?: string, max = 60) {
    if (!value) return "-";
    if (value.length <= max) return value;
    return `${value.slice(0, max)}...`;
}

export default function SchedulerPage() {
    const [searchForm] = Form.useForm<UnitFilterForm>();
    const [logForm] = Form.useForm<LogFilterForm>();
    const { token } = theme.useToken();
    const { t } = useI18n();

    const [namespaceLoading, setNamespaceLoading] = useState(false);
    const [namespaces, setNamespaces] = useState<NamespaceVO[]>([]);
    const [executorOptions, setExecutorOptions] = useState<string[]>([]);
    const [groupOptions, setGroupOptions] = useState<string[]>([]);
    const [relationLoading, setRelationLoading] = useState(false);

    const [filters, setFilters] = useState<UnitFilters>({ namespaceId: "" });
    const [unitsLoading, setUnitsLoading] = useState(false);
    const [units, setUnits] = useState<JobUnitVO[]>([]);
    const [unitsTotal, setUnitsTotal] = useState(0);
    const [unitsPageIndex, setUnitsPageIndex] = useState(1);
    const [unitsPageSize, setUnitsPageSize] = useState(10);
    const [switchingUnitId, setSwitchingUnitId] = useState<string>("");

    const [nodeModalOpen, setNodeModalOpen] = useState(false);
    const [nodeLoading, setNodeLoading] = useState(false);
    const [nodeUnit, setNodeUnit] = useState<JobUnitVO | null>(null);
    const [nodes, setNodes] = useState<ExecutorNodeVO[]>([]);

    const [logModalOpen, setLogModalOpen] = useState(false);
    const [logLoading, setLogLoading] = useState(false);
    const [logUnit, setLogUnit] = useState<JobUnitVO | null>(null);
    const [records, setRecords] = useState<JobRecordVO[]>([]);
    const [recordsTotal, setRecordsTotal] = useState(0);
    const [logPageIndex, setLogPageIndex] = useState(1);
    const [logPageSize, setLogPageSize] = useState(10);
    const [logFilters, setLogFilters] = useState<LogFilterForm>({});
    const [selectedRecordIds, setSelectedRecordIds] = useState<string[]>([]);
    const [deletingRecords, setDeletingRecords] = useState(false);
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

    const executorSelectOptions = useMemo(
        () => executorOptions.map((item) => ({ label: item, value: item })),
        [executorOptions],
    );

    const groupSelectOptions = useMemo(
        () => groupOptions.map((item) => ({ label: item, value: item })),
        [groupOptions],
    );

    const namespaceTabs = useMemo(
        () =>
            namespaces.map((item) => ({
                key: item.id,
                label: item.name || item.id,
            })),
        [namespaces],
    );

    const loadNamespaceRelations = async (namespaceId: string) => {
        if (!namespaceId) {
            setExecutorOptions([]);
            setGroupOptions([]);
            return;
        }
        setRelationLoading(true);
        try {
            const [executors, groups] = await Promise.all([
                schedulingApi.jobUnitExecutorNames(namespaceId),
                schedulingApi.jobUnitGroups(namespaceId),
            ]);
            setExecutorOptions(executors ?? []);
            setGroupOptions(groups ?? []);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.loadFiltersFailed"));
        } finally {
            setRelationLoading(false);
        }
    };

    const loadUnits = async (nextFilters: UnitFilters, pageIndex = unitsPageIndex, pageSize = unitsPageSize) => {
        if (!nextFilters.namespaceId) {
            setUnits([]);
            setUnitsTotal(0);
            return;
        }
        setUnitsLoading(true);
        try {
            const res = await schedulingApi.jobUnitSearchPage({
                pageIndex,
                pageSize,
                eqNamespaceId: nextFilters.namespaceId,
                // 后端当前将执行器与组字段映射反了，这里做兼容转换。
                eqExecutorName: nextFilters.groupName || undefined,
                eqGroupName: nextFilters.executorName || undefined,
                likeName: nextFilters.unitName?.trim() || undefined,
            });
            setUnits(res.data ?? []);
            setUnitsTotal(Number(res.total ?? 0));
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.loadUnitsFailed"));
        } finally {
            setUnitsLoading(false);
        }
    };

    const applyNamespace = async (namespaceId: string) => {
        const nextFilters: UnitFilters = { namespaceId, executorName: undefined, groupName: undefined, unitName: "" };
        searchForm.setFieldsValue({ executorName: undefined, groupName: undefined, unitName: "" });
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        setUnitsPageSize(10);
        await loadNamespaceRelations(namespaceId);
        await loadUnits(nextFilters, 1, 10);
    };

    const refreshNamespaces = async () => {
        setNamespaceLoading(true);
        try {
            const list = await namespaceApi.namespaceAll();
            const rows = list ?? [];
            setNamespaces(rows);
            const nextNamespaceId = filters.namespaceId || rows[0]?.id || "";
            if (nextNamespaceId) {
                await applyNamespace(nextNamespaceId);
            } else {
                setExecutorOptions([]);
                setGroupOptions([]);
                setUnits([]);
                setUnitsTotal(0);
            }
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.loadNamespacesFailed"));
        } finally {
            setNamespaceLoading(false);
        }
    };

    useEffect(() => {
        void refreshNamespaces();
    }, []);

    const handleSearch = async () => {
        const values = searchForm.getFieldsValue();
        const nextFilters: UnitFilters = {
            namespaceId: filters.namespaceId,
            executorName: values.executorName || undefined,
            groupName: values.groupName || undefined,
            unitName: values.unitName?.trim() || "",
        };
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        await loadUnits(nextFilters, 1, unitsPageSize);
    };

    const handleReset = async () => {
        const namespaceId = filters.namespaceId;
        searchForm.setFieldsValue({
            executorName: undefined,
            groupName: undefined,
            unitName: "",
        });
        const nextFilters: UnitFilters = { namespaceId, executorName: undefined, groupName: undefined, unitName: "" };
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        await loadUnits(nextFilters, 1, unitsPageSize);
    };

    const handleToggleEnable = async (row: JobUnitVO, checked: boolean) => {
        setSwitchingUnitId(row.id);
        try {
            if (checked) {
                await schedulingApi.jobUnitEnable(row.id);
            } else {
                await schedulingApi.jobUnitDisable(row.id);
            }
            message.success(checked ? t("scheduler.enableSuccess") : t("scheduler.disableSuccess"));
            await loadUnits(filters, unitsPageIndex, unitsPageSize);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.updateEnableFailed"));
        } finally {
            setSwitchingUnitId("");
        }
    };

    const handleDeleteUnit = async (row: JobUnitVO) => {
        try {
            await schedulingApi.jobUnitDelete(row.id);
            message.success(t("scheduler.deleteUnitSuccess"));
            const nextPage = units.length === 1 && unitsPageIndex > 1 ? unitsPageIndex - 1 : unitsPageIndex;
            setUnitsPageIndex(nextPage);
            await loadUnits(filters, nextPage, unitsPageSize);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.deleteUnitFailed"));
        }
    };

    const openNodeModal = async (row: JobUnitVO) => {
        setNodeUnit(row);
        setNodeModalOpen(true);
        setNodeLoading(true);
        try {
            const res = await schedulingApi.executorNodeSearchPage({
                pageIndex: 1,
                pageSize: -1,
                eqNamespaceId: row.namespaceId,
                eqExecutorName: row.executorName,
                eqGroupName: row.groupName,
            });
            setNodes(res.data ?? []);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.loadNodesFailed"));
        } finally {
            setNodeLoading(false);
        }
    };

    const loadLogs = async (unit: JobUnitVO, nextFilters = logFilters, pageIndex = logPageIndex, pageSize = logPageSize) => {
        setLogLoading(true);
        try {
            const res = await schedulingApi.jobRecordSearchPage({
                pageIndex,
                pageSize,
                eqNamespaceId: unit.namespaceId,
                eqExecutorName: unit.executorName,
                eqGroupName: unit.groupName,
                eqUnitId: unit.id,
                eqCode: nextFilters.code,
            });
            setRecords(res.data ?? []);
            setRecordsTotal(Number(res.total ?? 0));
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.loadLogsFailed"));
        } finally {
            setLogLoading(false);
        }
    };

    const openLogModal = async (row: JobUnitVO) => {
        setLogUnit(row);
        setLogModalOpen(true);
        logForm.setFieldsValue({ code: undefined });
        setLogFilters({});
        setSelectedRecordIds([]);
        setLogPageIndex(1);
        setLogPageSize(10);
        await loadLogs(row, {}, 1, 10);
    };

    const deleteSelectedRecords = async () => {
        if (!logUnit) return;
        if (selectedRecordIds.length === 0) return;
        setDeletingRecords(true);
        try {
            await schedulingApi.jobRecordDelete(selectedRecordIds);
            message.success(t("scheduler.deleteLogsSuccess", { count: selectedRecordIds.length }));
            const removedInCurrentPage = (records ?? []).filter((r) => selectedRecordIds.includes(r.id)).length;
            const nextPage = removedInCurrentPage === (records ?? []).length && logPageIndex > 1 ? logPageIndex - 1 : logPageIndex;
            setSelectedRecordIds([]);
            setLogPageIndex(nextPage);
            await loadLogs(logUnit, logFilters, nextPage, logPageSize);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("scheduler.deleteLogsFailed"));
        } finally {
            setDeletingRecords(false);
        }
    };

    const unitColumns: ColumnsType<JobUnitVO> = [
        { title: t("scheduler.unitName"), dataIndex: "name", key: "name", },
        { title: t("scheduler.executorName"), dataIndex: "executorName", key: "executorName", },
        { title: t("scheduler.groupName"), dataIndex: "groupName", key: "groupName", },
        {
            title: t("scheduler.unitType"),
            dataIndex: "type",
            key: "type",
            render: (value) => <Tag>{unitTypeLabel(value, t)}</Tag>,
        },
        {
            title: t("scheduler.typeValue"),
            dataIndex: "typeValue",
            key: "typeValue",
            render: (value, row) => {
                if (!value) return "-";
                if (row.type !== 2) return value + "s";
                const preview = getNextThreeCronTimes(value, t);
                const tooltipTitle = preview.ok ? (
                    <Space direction="vertical">
                        {preview.list.map((item) => (
                            <Tag color="green">{item}</Tag>
                        ))}
                    </Space>
                ) : (
                    <Typography.Text type="danger">{preview.error}</Typography.Text>
                );
                return (
                    <Tooltip
                        title={tooltipTitle}
                        placement="topLeft"
                        color={token.colorBgElevated}
                        overlayInnerStyle={{ color: token.colorText, border: `1px solid ${token.colorBorderSecondary}` }}
                    >
                        <Tag color="red">{value}</Tag>
                    </Tooltip>
                );
            },
        },
        {
            title: t("scheduler.lastPlanTime"),
            dataIndex: "lastPlanTime",
            key: "lastPlanTime",
            render: (value) => formatTs(value),
        },
        {
            title: t("scheduler.enabled"),
            dataIndex: "enable",
            key: "enable",
            render: (value, row) => (
                <Switch
                    checked={value === 0}
                    loading={switchingUnitId === row.id}
                    onChange={(checked) => void handleToggleEnable(row, checked)}
                />
            ),
        },
        {
            title: t("common.actions"),
            key: "actions",
            render: (_, row) => (
                <Space wrap>
                    <Button size="small" onClick={() => void openNodeModal(row)}>
                        {t("scheduler.executorNodes")}
                    </Button>
                    <Button size="small" onClick={() => void openLogModal(row)}>
                        {t("scheduler.logs")}
                    </Button>
                    <Popconfirm title={t("scheduler.deleteConfirm")} onConfirm={() => void handleDeleteUnit(row)}>
                        <Button danger size="small">
                            {t("common.delete")}
                        </Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    const nodeColumns: ColumnsType<ExecutorNodeVO> = [
        { title: t("scheduler.ip"), dataIndex: "ip", key: "ip", width: 160 },
        { title: t("scheduler.port"), dataIndex: "port", key: "port", width: 100 },
        {
            title: t("scheduler.secret"),
            dataIndex: "secret",
            key: "secret",
            render: (value) => maskSecret(value),
        },
        {
            title: t("common.updateTime"),
            dataIndex: "updateTime",
            key: "updateTime",
            render: (value) => formatTs(value),
        },
    ];

    const logColumns: ColumnsType<JobRecordVO> = [
        {
            title: t("scheduler.planStartTime"),
            dataIndex: "planStartTime",
            key: "planStartTime",
            render: (value) => formatTsMs(value),
        },
        {
            title: t("scheduler.actualStartTime"),
            dataIndex: "actualStartTime",
            key: "actualStartTime",
            render: (value) => formatTsMs(value),
        },
        {
            title: t("scheduler.actualEndTime"),
            dataIndex: "actualEndTime",
            key: "actualEndTime",
            render: (value) => formatTsMs(value),
        },
        {
            title: t("scheduler.duration"),
            key: "duration",
            render: (_, row) => durationMs(row.actualStartTime, row.actualEndTime),
        },
        {
            title: t("scheduler.statusCode"),
            dataIndex: "code",
            key: "code",
            render: (value) => codeTag(value, t),
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

    const logPagination: TablePaginationConfig = {
        current: logPageIndex,
        pageSize: logPageSize,
        total: recordsTotal,
        showSizeChanger: true,
        onChange: (page, pageSize) => {
            if (!logUnit) return;
            setLogPageIndex(page);
            setLogPageSize(pageSize ?? 10);
            void loadLogs(logUnit, logFilters, page, pageSize ?? 10);
        },
    };

    return (
        <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Card bordered={false} bodyStyle={{ paddingBottom: 8 }}>
                {namespaceTabs.length > 0 ? (
                    <Tabs
                        activeKey={filters.namespaceId || undefined}
                        items={namespaceTabs}
                        onChange={(key) => void applyNamespace(key)}
                    />
                ) : (
                    <Empty description={t("common.noNamespace")} />
                )}
            </Card>

            <Card>
                <Form form={searchForm} layout="inline" onFinish={() => void handleSearch()}>
                    <Form.Item label={t("scheduler.executorName")} name="executorName">
                        <Select
                            allowClear
                            placeholder={t("scheduler.search.executorPlaceholder")}
                            style={{ width: 220 }}
                            loading={relationLoading}
                            options={executorSelectOptions}
                            disabled={!filters.namespaceId}
                        />
                    </Form.Item>
                    <Form.Item label={t("scheduler.groupName")} name="groupName">
                        <Select
                            allowClear
                            placeholder={t("scheduler.search.groupPlaceholder")}
                            style={{ width: 220 }}
                            loading={relationLoading}
                            options={groupSelectOptions}
                            disabled={!filters.namespaceId}
                        />
                    </Form.Item>
                    <Form.Item label={t("scheduler.unitName")} name="unitName">
                        <Input allowClear placeholder={t("scheduler.search.unitPlaceholder")} style={{ width: 240 }} disabled={!filters.namespaceId} />
                    </Form.Item>
                    <Form.Item>
                        <Space>
                            <Button type="primary" htmlType="submit" disabled={!filters.namespaceId}>
                                {t("common.search")}
                            </Button>
                            <Button onClick={() => void handleReset()} disabled={!filters.namespaceId}>
                                {t("common.reset")}
                            </Button>
                        </Space>
                    </Form.Item>
                </Form>
            </Card>

            {!filters.namespaceId ? (
                <Card bordered={false}>
                    <Empty description={t("common.selectNamespaceFirst")} />
                </Card>
            ) : (
                <Card
                    bordered={false}
                    title={
                        <Space wrap>
                            <Typography.Text>
                                {t("common.currentNamespace")}
                                {(namespaces.find((item) => item.id === filters.namespaceId)?.name || filters.namespaceId) ?? "-"}
                            </Typography.Text>
                            <Typography.Text type="secondary">{t("scheduler.totalUnits", { count: unitsTotal })}</Typography.Text>
                        </Space>
                    }
                    extra={
                        <Button onClick={() => void loadUnits(filters, unitsPageIndex, unitsPageSize)} loading={unitsLoading}>
                            {t("scheduler.refreshList")}
                        </Button>
                    }
                >
                    <Table rowKey="id" loading={unitsLoading} dataSource={units} columns={unitColumns} pagination={unitPagination} scroll={{ x: 1500 }} />
                </Card>
            )}

            <Modal
                title={t("scheduler.nodeModalTitle", { name: nodeUnit?.name || "" })}
                open={nodeModalOpen}
                onCancel={() => setNodeModalOpen(false)}
                footer={<Button onClick={() => setNodeModalOpen(false)}>{t("common.close")}</Button>}>
                <Table rowKey="id" loading={nodeLoading} dataSource={nodes} columns={nodeColumns} pagination={false} locale={{ emptyText: t("scheduler.noExecutorNodes") }} />
            </Modal>

            <Modal
                title={t("scheduler.logModalTitle", { name: logUnit?.name || "" })}
                open={logModalOpen}
                onCancel={() => setLogModalOpen(false)}
                footer={
                    <Space>
                        <Popconfirm
                            title={t("scheduler.deleteSelectedConfirm", { count: selectedRecordIds.length })}
                            onConfirm={() => void deleteSelectedRecords()}
                            okButtonProps={{ danger: true }}
                            okText={t("common.delete")}
                            cancelText={t("common.cancel")}
                            disabled={selectedRecordIds.length === 0}
                        >
                            <Button danger disabled={selectedRecordIds.length === 0} loading={deletingRecords}>
                                {t("scheduler.deleteSelected")}
                            </Button>
                        </Popconfirm>
                        <Button onClick={() => setLogModalOpen(false)}>{t("common.close")}</Button>
                    </Space>
                }
                width={logModalWidth}
            >
                <Space direction="vertical" size={12} style={{ width: "100%" }}>
                    <Card size="small">
                        <Space wrap>
                            <Typography.Text type="secondary">{t("scheduler.namespaceLabel")}</Typography.Text>
                            <Typography.Text>{logUnit?.namespaceId || "-"}</Typography.Text>
                            <Typography.Text type="secondary">{t("scheduler.executorLabel")}</Typography.Text>
                            <Typography.Text>{logUnit?.executorName || "-"}</Typography.Text>
                            <Typography.Text type="secondary">{t("scheduler.groupLabel")}</Typography.Text>
                            <Typography.Text>{logUnit?.groupName || "-"}</Typography.Text>
                            <Typography.Text type="secondary">{t("scheduler.unitLabel")}</Typography.Text>
                            <Typography.Text>{logUnit?.name || "-"}</Typography.Text>
                        </Space>
                    </Card>

                    <Form
                        form={logForm}
                        layout="inline"
                        style={{ display: "flex", flexWrap: "wrap", rowGap: 8, columnGap: 8 }}
                        onFinish={() => {
                            if (!logUnit) return;
                            const nextFilters = logForm.getFieldsValue();
                            setLogFilters(nextFilters);
                            setLogPageIndex(1);
                            void loadLogs(logUnit, nextFilters, 1, logPageSize);
                        }}
                    >
                        <Form.Item label={t("scheduler.statusCode")} name="code">
                            <Select
                                allowClear
                                placeholder={t("common.all")}
                                style={{ width: 180 }}
                                options={[
                                    { label: t("scheduler.codeOption.0"), value: 0 },
                                    { label: t("scheduler.codeOption.200"), value: 200 },
                                    { label: t("scheduler.codeOption.400"), value: 400 },
                                    { label: t("scheduler.codeOption.403"), value: 403 },
                                    { label: t("scheduler.codeOption.500"), value: 500 },
                                ]}
                            />
                        </Form.Item>
                        <Form.Item>
                            <Space>
                                <Button type="primary" htmlType="submit" loading={logLoading} disabled={!logUnit}>
                                    {t("common.search")}
                                </Button>
                                <Button
                                    disabled={!logUnit || logLoading}
                                    onClick={() => {
                                        if (!logUnit) return;
                                        logForm.setFieldsValue({ code: undefined });
                                        setLogFilters({});
                                        setLogPageIndex(1);
                                        void loadLogs(logUnit, {}, 1, logPageSize);
                                    }}
                                >
                                    {t("common.reset")}
                                </Button>
                            </Space>
                        </Form.Item>
                    </Form>

                    <Table
                        rowKey="id"
                        loading={logLoading}
                        dataSource={records}
                        columns={logColumns}
                        rowSelection={{
                            selectedRowKeys: selectedRecordIds,
                            preserveSelectedRowKeys: true,
                            onChange: (keys) => setSelectedRecordIds(keys as string[]),
                        }}
                        pagination={logPagination}
                        tableLayout="auto"
                        expandable={{
                            expandedRowRender: (row) => (
                                <Space direction="vertical" size={8} style={{ width: "100%" }}>
                                    <Typography.Text type="secondary">{t("scheduler.executorNodeLabel", { value: row.ip + ":" + row.port || "-" })}</Typography.Text>
                                    <Typography.Text type="secondary">
                                        {t("scheduler.secretLabel", { value: maskSecret(row.secret) })}
                                    </Typography.Text>
                                    <Typography.Text type="secondary">{t("scheduler.messageLabel", { value: row.message || "-" })}</Typography.Text>
                                    {(row.result !== "") && (<Typography.Text type="secondary">{t("scheduler.resultLabel", { value: row.result || "-" })}</Typography.Text>)}
                                    {(row.log ?? []).length > 0 && (<><Typography.Text type="secondary">{t("scheduler.logLabel")}</Typography.Text>
                                        <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: "pre-wrap" }}>
                                            {(row.log ?? []).length > 0 ? row.log?.join("\n") : "-"}
                                        </Typography.Paragraph>
                                    </>)}
                                </Space>
                            ),
                            rowExpandable: (row) => Boolean(row.message || row.result || (row.log ?? []).length),
                        }}
                    />
                </Space>
            </Modal>
        </Space>
    );
}
