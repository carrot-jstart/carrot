import { Button, Card, Form, Input, Modal, Popconfirm, Space, Table, message } from "antd";
import React, { useEffect, useState } from "react";
import type { ColumnsType } from "antd/es/table";
import type { NamespaceDTO, NamespaceVO } from "../api/types";
import { namespaceApi } from "../api/namespace";
import { formatTs } from "../utils/time";
import { useI18n } from "../i18n/useI18n";

type ModalMode = "add" | "edit";

type ModalFormValues = {
  id?: string;
  name: string;
  description: string;
};

export default function NamespacePage() {
  const [modalForm] = Form.useForm<ModalFormValues>();
  const { t } = useI18n();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<NamespaceVO[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<ModalMode>("add");
  const [modalLoading, setModalLoading] = useState(false);
  const [editingRow, setEditingRow] = useState<NamespaceVO | null>(null);

  const refresh = async () => {
    setLoading(true);
    try {
      const rows = await namespaceApi.namespaceAll();
      setData(rows ?? []);
    } catch (e) {
      message.error(e instanceof Error ? e.message : t("namespace.loadFailed"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const openAdd = () => {
    setModalMode("add");
    setEditingRow(null);
    modalForm.resetFields();
    modalForm.setFieldsValue({ id: "", name: "", description: "" });
    setModalOpen(true);
  };

  const openEdit = (row: NamespaceVO) => {
    setModalMode("edit");
    setEditingRow(row);
    modalForm.resetFields();
    modalForm.setFieldsValue({ id: row.id, name: row.name, description: row.description });
    setModalOpen(true);
  };

  const onDelete = async (id: string) => {
    try {
      await namespaceApi.namespaceDelete(id);
      message.success(t("namespace.deleted"));
      await refresh();
    } catch (e) {
      message.error(e instanceof Error ? e.message : t("namespace.deleteFailed"));
    }
  };

  const submitModal = async () => {
    const values = await modalForm.validateFields();
    const payload: NamespaceDTO = {
      id: values.id?.trim() || undefined,
      name: values.name.trim(),
      description: values.description.trim(),
    };

    setModalLoading(true);
    try {
      if (modalMode === "add") {
        await namespaceApi.namespaceAdd(payload);
        message.success(t("namespace.created"));
      } else {
        await namespaceApi.modifyNamespace({
          id: editingRow?.id,
          name: payload.name,
          description: payload.description,
        });
        message.success(t("namespace.saved"));
      }
      setModalOpen(false);
      await refresh();
    } catch (e) {
      if (e && typeof e === "object" && "errorFields" in (e as any)) {
        return;
      }
      message.error(e instanceof Error ? e.message : t("namespace.submitFailed"));
    } finally {
      setModalLoading(false);
    }
  };

  const columns: ColumnsType<NamespaceVO> = [
    { title: t("namespace.id"), dataIndex: "id", key: "id" },
    { title: t("namespace.name"), dataIndex: "name", key: "name" },
    { title: t("common.createTime"), dataIndex: "createTime", key: "createTime", render: (v) => formatTs(v) },
    { title: t("namespace.description"), dataIndex: "description", key: "description" },
    {
      title: t("common.actions"),
      key: "actions",
      width: 160,
      render: (_, row) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>
            {t("common.edit")}
          </Button>
          <Popconfirm
            title={t("namespace.deleteConfirm")}
            okText={t("common.confirm")}
            cancelText={t("common.cancel")}
            onConfirm={() => onDelete(row.id)}
          >
            <Button danger size="small">
              {t("common.delete")}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title={t("namespace.title")}
      bordered={false}
      extra={
        <Space>
          <Button onClick={refresh} disabled={loading}>
            {t("namespace.refresh")}
          </Button>
          <Button type="primary" onClick={openAdd}>
            {t("namespace.create")}
          </Button>
        </Space>
      }
    >
      <Table rowKey="id" loading={loading} dataSource={data} columns={columns} pagination={false} />
      <Modal
        title={modalMode === "add" ? t("namespace.createTitle") : t("namespace.editTitle")}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        cancelText={t("common.cancel")}
        okText={t("common.confirm")}
        onOk={submitModal}
        confirmLoading={modalLoading}
        destroyOnClose
      >
        <Form form={modalForm} layout="vertical">
          <Form.Item
            label="ID"
            name="id"
            rules={[
              {
                pattern: /^[A-Za-z0-9_-]*$/,
                message: t("namespace.idRule"),
              },
            ]}
          >
            <Input
              placeholder={t("namespace.idPlaceholder")}
              disabled={modalMode === "edit"}
              onChange={(e) => {
                const sanitized = e.target.value.replace(/[^A-Za-z0-9_-]/g, "");
                if (sanitized !== e.target.value) {
                  modalForm.setFieldValue("id", sanitized);
                }
              }}
            />
          </Form.Item>
          <Form.Item label={t("common.name")} name="name" rules={[{ required: true, message: t("namespace.nameRequired") }]}>
            <Input placeholder={t("namespace.namePlaceholder")} />
          </Form.Item>
          <Form.Item
            label={t("common.description")}
            name="description"
            rules={[{ required: true, message: t("namespace.descriptionRequired") }]}
          >
            <Input.TextArea placeholder={t("namespace.descriptionPlaceholder")} autoSize={{ minRows: 3, maxRows: 6 }} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
