import { postJson, requestJson } from "./client";
import type {
  AdminVO,
  ExecutorNodeVO,
  JobRecordVO,
  JobUnitVO,
  PageResult,
  NamespaceDTO,
  NamespaceVO,
} from "./types";

export type SearchBasic = {
  pageSize: number;
  pageIndex: number;
};

export type SearchJobUnit = SearchBasic & {
  eqNamespaceId?: string;
  eqExecutorName?: string;
  eqGroupName?: string;
  likeName?: string;
};

export type SearchJobRecord = SearchBasic & {
  eqNamespaceId?: string;
  eqExecutorName?: string;
  eqGroupName?: string;
  eqUnitId?: string;
  eqCode?: number;
  planStartTimeMin?: number;
  planStartTimeMax?: number;
};

export type SearchExecutorNode = SearchBasic & {
  eqNamespaceId?: string;
  eqExecutorName?: string;
  eqGroupName?: string;
};

function appendIfPresent(params: URLSearchParams, key: string, value: string | number | boolean | undefined) {
  if (value === undefined || value === null || value === "") return;
  params.set(key, String(value));
}

export const schedulingApi = {
  adminAll: () => postJson<AdminVO[]>("/admin/node/all"),

  namespaceAll: () => postJson<NamespaceVO[]>("/admin/namespace/all"),
  namespaceAdd: async (req: NamespaceDTO) => {
    const body = new URLSearchParams();
    if (req.id) body.set("id", req.id);
    body.set("name", req.name);
    body.set("description", req.description);
    return requestJson<string>("/admin/namespace/create", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: body.toString(),
    });
  },
  modifyNamespace: async (req: NamespaceDTO) => {
    const body = new URLSearchParams();
    if (req.id) body.set("id", req.id);
    body.set("name", req.name);
    body.set("description", req.description);
    return requestJson<string>("/admin/namespace/modify", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: body.toString(),
    });
  },
  namespaceDelete: async (namespaceId: string) =>
    requestJson<unknown>(`/admin/namespace/delete?id=${encodeURIComponent(namespaceId)}`, { method: "POST" }),

  jobUnitExecutorNames: async (namespaceId: string) =>
    requestJson<string[]>(
      `/scheduling/job/unit/get/executor?namespaceId=${encodeURIComponent(namespaceId)}`,
      { method: "POST" },
    ),
  jobUnitGroups: async (namespaceId: string) =>
    requestJson<string[]>(
      `/scheduling/job/unit/get/group?namespaceId=${encodeURIComponent(namespaceId)}`,
      { method: "POST" },
    ),
  jobUnitSearchPage: async (req: SearchJobUnit) =>
    postJson<PageResult<JobUnitVO[]>>("/scheduling/job/unit/search/page", req),
  jobUnitEnable: async (id: string) =>
    requestJson<string>(`/scheduling/job/unit/enable?id=${encodeURIComponent(id)}`, { method: "POST" }),
  jobUnitDisable: async (id: string) =>
    requestJson<string>(`/scheduling/job/unit/disable?id=${encodeURIComponent(id)}`, { method: "POST" }),
  jobUnitGet: async (id: string) =>
    requestJson<JobUnitVO>(`/scheduling/job/unit/get?id=${encodeURIComponent(id)}`, { method: "POST" }),
  jobUnitDelete: async (id: string) =>
    requestJson<string>(`/scheduling/job/unit/delete?id=${encodeURIComponent(id)}`, { method: "POST" }),

  jobRecordSearchPage: async (req: SearchJobRecord) =>
    postJson<PageResult<JobRecordVO[]>>("/scheduling/job/record/search/page", req),
  jobRecordGet: async (id: string) =>
    requestJson<JobRecordVO>(`/scheduling/job/record/get?id=${encodeURIComponent(id)}`, { method: "POST" }),
  jobRecordDelete: async (ids: string[]) =>
    postJson<string>("/scheduling/job/record/delete", ids),

  executorNodeSearchPage: async (req: SearchExecutorNode) => {
    return requestJson<PageResult<ExecutorNodeVO[]>>(`/scheduling/executor/node/search/page`, {
      body: JSON.stringify(req),
      method: "POST",
    });
  },
};
