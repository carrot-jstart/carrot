import { Button, Card, Empty, Form, Input, Modal, Space, Table, Tabs, Tooltip, Typography, message } from "antd";
import { EyeOutlined } from "@ant-design/icons";
import type { ColumnsType, TablePaginationConfig } from "antd/es/table";
import dayjs from "dayjs";
import React, { useEffect, useMemo, useState } from "react";
import { namespaceApi } from "../api/namespace";
import type { DiscoveryServiceVO, DiscoveryInstanceVO, NamespaceVO, KeyValue } from "../api/types";
import { discoveryApi } from "../api/discovery";
import { useI18n } from "../i18n/useI18n";

type UnitFilterForm = {
    likeServiceName?: string;
    likeGroup?: string;
};

type UnitFilters = {
    eqNamespace: string;
    likeGroup?: string;
    likeServiceName?: string;
};


export default function DiscoveryPage() {
    const [searchForm] = Form.useForm<UnitFilterForm>();
    const { t } = useI18n();
    const [namespaces, setNamespaces] = useState<NamespaceVO[]>([]);
    const [filters, setFilters] = useState<UnitFilters>({ eqNamespace: "" });
    const [unitsLoading, setUnitsLoading] = useState(false);
    const [units, setServuces] = useState<DiscoveryServiceVO[]>([]);
    const [unitsTotal, setUnitsTotal] = useState(0);
    const [unitsPageIndex, setUnitsPageIndex] = useState(1);
    const [unitsPageSize, setUnitsPageSize] = useState(10);
    const [instanceModalOpen, setInstanceModalOpen] = useState(false);
    const [instanceLoading, setInstanceLoading] = useState(false);
    const [instances, setInstances] = useState<DiscoveryInstanceVO[]>([]);
    const [selectsService, setSelectsService] = useState<DiscoveryServiceVO | undefined>(undefined);
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
            const res = await discoveryApi.pageDiscoveryService({
                pageIndex,
                pageSize,
                eqNamespace: nextFilters.eqNamespace,
                // 后端当前将执行器与组字段映射反了，这里做兼容转换。
                likeGroup: nextFilters.likeGroup || undefined,
                likeServiceName: nextFilters.likeServiceName || undefined,
            });
            setServuces(res.data ?? []);
            setUnitsTotal(Number(res.total ?? 0));
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("discovery.loadUnitsFailed"));
        } finally {
            setUnitsLoading(false);
        }
    };

    const applyNamespace = async (namespaceId: string) => {
        const nextFilters: UnitFilters = { eqNamespace: namespaceId, likeGroup: undefined, likeServiceName: undefined };
        searchForm.setFieldsValue({ likeGroup: undefined, likeServiceName: undefined });
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
            message.error(e instanceof Error ? e.message : t("discovery.loadNamespacesFailed"));
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
            likeServiceName: values.likeServiceName || undefined,
        };
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        await loadUnits(nextFilters, 1, unitsPageSize);
    };

    const handleReset = async () => {
        const namespaceId = filters.eqNamespace;
        searchForm.setFieldsValue({
            likeServiceName: undefined,
            likeGroup: undefined
        });
        const nextFilters: UnitFilters = { eqNamespace: namespaceId, likeGroup: undefined, likeServiceName: undefined };
        setFilters(nextFilters);
        setUnitsPageIndex(1);
        await loadUnits(nextFilters, 1, unitsPageSize);
    };

    const instanceColumns: ColumnsType<DiscoveryInstanceVO> = [
        {
            title: t("discovery.instanceId"), dataIndex: "instanceId", key: "instanceId",
        },
        { title: t("discovery.instanceIp"), dataIndex: "ip", key: "ip", },
        { title: t("discovery.instancePort"), dataIndex: "port", key: "port", },
        { title: t("discovery.instanceWeight"), dataIndex: "weight", key: "weight", },
        {
            title: t("discovery.instanceHeartbeatTime"), dataIndex: "lastHeartbeatAt", key: "lastHeartbeatAt",
            render: (v) => {
                const d = dayjs(v);
                return d.isValid() ? d.format("YYYY-MM-DD HH:mm:ss") : "-";
            },
        },
        {
            title: t("discovery.instanceMetadata"), dataIndex: "metadata", key: "metadata",
            render: (v: unknown) => {
                if (!v) return "-";
                if (Array.isArray(v)) return v.map((item: KeyValue<string, string>) => `${item.key}=${item.value}`).join(", ") || "-";
                if (typeof v === "object") return Object.entries(v as Record<string, unknown>).map(([k, val]) => `${k}=${String(val)}`).join(", ") || "-";
                return String(v);
            },
        },
    ];

    const openInstaceModal = async (row: DiscoveryServiceVO) => {
        setSelectsService(row);
        setInstanceModalOpen(true);
        setInstanceLoading(true);
        try {
            const res = await discoveryApi.pageDiscoveryInstance({
                pageIndex: 1,
                pageSize: -1,
                eqNamespace: row.namespace,
                eqGroup: row.group,
                eqServiceName: row.serviceName,
            });
            setInstances(res.data ?? []);
        } catch (e) {
            message.error(e instanceof Error ? e.message : t("discovery.loadInstancesFailed"));
        } finally {
            setInstanceLoading(false);
        }
    };

    const serviceColumns: ColumnsType<DiscoveryServiceVO> = [
        { title: t("discovery.serviceName"), dataIndex: "serviceName", key: "serviceName", },
        {
            title: t("discovery.group"),
            dataIndex: "group",
            key: "group",
        },
        {
            title: t("discovery.instanceCount"),
            dataIndex: "instanceCount",
            key: "instanceCount",
        },
        {
            title: t("common.actions"),
            key: "actions",
            render: (_, row) => (
                <Space wrap size="small" style={{ justifyContent: "center", width: "100%" }}>

                    <Tooltip title={t("discovery.viewInstances")}>
                        <Button
                            size="small"
                            type="text"
                            icon={<EyeOutlined />}
                            onClick={() => { void openInstaceModal(row); }}
                        />
                    </Tooltip>
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
                            {(namespaces.find((item) => item.id === filters.eqNamespace)?.name || filters.eqNamespace) ?? "-"}
                        </Typography.Text>
                    </Space>
                }
            >
                <Form form={searchForm} layout="inline" onFinish={() => void handleSearch()}>
                    <Form.Item label={t("discovery.search.serviceLabel")} name="likeServiceName">
                        <Input allowClear placeholder={t("discovery.search.servicePlaceholder")} style={{ width: 240 }} disabled={!filters.eqNamespace} />
                    </Form.Item>
                    <Form.Item label={t("discovery.search.groupLabel")} name="likeGroup">
                        <Input allowClear placeholder={t("discovery.search.groupPlaceholder")} style={{ width: 240 }} disabled={!filters.eqNamespace} />
                    </Form.Item>
                    <Form.Item>
                        <Space>
                            <Button type="primary" htmlType="submit" disabled={!filters.eqNamespace}>
                                {t("common.search")}
                            </Button>
                            <Button onClick={() => void handleReset()} disabled={!filters.eqNamespace}>
                                {t("common.reset")}
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
                    <Table
                        rowKey={(row) => `${row.namespace}@@${row.group}@@${row.serviceName}`}
                        loading={unitsLoading}
                        dataSource={units}
                        columns={serviceColumns}
                        pagination={unitPagination}
                        scroll={{ x: 1500 }}
                    />
                </Card>
            )}
            <Modal
                title={t("discovery.modalTitle", {
                    namespace: selectsService?.namespace || "-",
                    serviceName: selectsService?.serviceName || "-",
                    group: selectsService?.group || "-"
                })}
                open={instanceModalOpen}
                width={logModalWidth}
                onCancel={() => setInstanceModalOpen(false)}
                footer={<Button onClick={() => { setSelectsService(undefined); setInstanceModalOpen(false); }}>{t("common.close")}</Button>}>
                <Table
                    rowKey={(row) => row.instanceId || `${row.ip}:${row.port}`}
                    loading={instanceLoading}
                    dataSource={instances}
                    columns={instanceColumns}
                    pagination={false}
                    locale={{ emptyText: t("discovery.noInstances") }}
                />
            </Modal>
        </Space>
    );
}
