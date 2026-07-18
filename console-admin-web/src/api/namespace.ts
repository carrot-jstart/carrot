import { postJson, requestJson } from "./client";
import type {
    NamespaceDTO,
    NamespaceVO,
} from "./types";

export type SearchBasic = {
    pageSize: number;
    pageIndex: number;
};
export const namespaceApi = {

    namespaceAll: () => postJson<NamespaceVO[]>("/namespace/all"),
    namespaceAdd: async (req: NamespaceDTO) => {
        return requestJson<string>("/namespace/create", {
            method: "POST",
            body: JSON.stringify(req),
        });
    },
    modifyNamespace: async (req: NamespaceDTO) => {
        return requestJson<string>(`/namespace/modify`, {
            body: JSON.stringify(req),
            method: "POST",
        });
    },
    namespaceDelete: async (namespaceId: string) =>
        requestJson<unknown>(`/namespace/delete?id=${encodeURIComponent(namespaceId)}`, { method: "POST" }),
};
