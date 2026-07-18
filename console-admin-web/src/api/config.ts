import { postJson, requestJson } from "./client";
import type {
    PageResult,
    ConfigItemVO,
    ConfigItemSimpleVO,
} from "./types";

export type SearchBasic = {
    pageSize: number;
    pageIndex: number;
};

export type SearchConfigItem = SearchBasic & {
    eqNamespace?: string;
    likeGroup?: string;
    likeDataId?: string;
};

export type SearchDiscoveryInstance = SearchBasic & {
    eqNamespace?: string;
    eqServiceName?: string;
    eqGroup?: string;
};

export type ModifyConfigItem = {
    id: string;
    content: string;
    contentType: string;
};

export type CreateConfigItem = {
    namespace: string;
    groupName: string;
    dataId: string;
    contentType: string;
    content: string;
};

export const configApi = {

    pageConfigItemSimple: async (req: SearchConfigItem) =>
        postJson<PageResult<ConfigItemSimpleVO[]>>("/config/search/simple/page", req),

    getById: async (id: string) => {
        return requestJson<ConfigItemVO>(`/config/get?id=${id}`, {
            method: "POST",
        });
    },
    modify: async (req: ModifyConfigItem) =>
        postJson<string>("/config/modify", req),
    create: async (req: CreateConfigItem) =>
        postJson<string>("/config/add", req),
    delete: async (id: string) =>
        requestJson<string>(`/config/delete?id=${id}`, {
            method: "POST",
        }),
};
