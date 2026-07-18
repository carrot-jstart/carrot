export type ExecutedResult<T> = {
  code: number;
  msg: string;
  data: T;
  msgCode?: string;
};

export type PageResult<T> = {
  limit: number;
  page: number;
  total: number;
  data: T;
};

export type AdminVO = {
  id: string;
  createTime: number;
  updateTime: number;
};

export type NamespaceDTO = {
  id?: string;
  name: string;
  description: string;
};
export type NamespaceVO = {
  id: string;
  name: string;
  createTime: number;
  description: string;
};

export type ExecutorNodeVO = {
  id: string;
  ip: string;
  port: number;
  updateTime: number;
  groupName: string;
  secret: string;
  executorName: string;
  namespaceId: string;
};

export type JobUnitVO = {
  id: string;
  name: string;
  executorName: string;
  groupName: string;
  type: number;
  typeValue: string;
  namespaceId: string;
  lastPlanTime?: number;
  hashValue?: number;
  enable: number;
};

export type JobRecordVO = {
  id: string;
  unitId: string;
  namespaceId: string;
  executorName: string;
  groupName: string;
  ip: string;
  port: number;
  secret: string;
  unitName: string;
  planStartTime?: number;
  actualStartTime?: number;
  actualEndTime?: number;
  code?: number;
  message?: string;
  result?: string;
  log?: string[];
  hashValue?: number;
};


export type KeyValue<K, V> = {
  key: K;
  value: V;
};

export type DiscoveryServiceVO = {
  namespace: string;
  group: string;
  serviceName: string;
  instanceCount: number;
};

export type DiscoveryInstanceVO = {
  namespace: string;
  group: string;
  serviceName: string;
  instanceId: string;
  ip: string;
  port: number;
  weight: number;
  lastHeartbeatAt: number;
  metadata: KeyValue<string, string>[];
};

export type ConfigItemSimpleVO = {
  id: string;
  namespace: string;
  group: string;
  dataId: string;
  data: string;
  updateTime: number;
  contentType: string;
}

export type ConfigItemVO = {
  id: string;
  namespace: string;
  group: string;
  dataId: string;
  content: string;
  groupName: string;
  md5: string;
  createTime: number;
  updateTime: number;
  contentType: string;
}
