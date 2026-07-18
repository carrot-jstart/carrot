import { postJson, requestJson } from "./client";
import type {
    PageResult,
    DiscoveryInstanceVO,
    DiscoveryServiceVO,
} from "./types";

export type SearchBasic = {
    pageSize: number;
    pageIndex: number;
};

export type SearchDiscoveryService = SearchBasic & {
    eqNamespace?: string;
    likeGroup?: string;
    likeServiceName?: string;
};

export type SearchDiscoveryInstance = SearchBasic & {
    eqNamespace?: string;
    eqServiceName?: string;
    eqGroup?: string;
};

export const discoveryApi = {

    pageDiscoveryService: async (req: SearchDiscoveryService) =>
        postJson<PageResult<DiscoveryServiceVO[]>>("/discovery/service/search", req),

    pageDiscoveryInstance: async (req: SearchDiscoveryInstance) => {
        return requestJson<PageResult<DiscoveryInstanceVO[]>>(`/discovery/instance/search`, {
            body: JSON.stringify(req),
            method: "POST",
        });
    },
};
