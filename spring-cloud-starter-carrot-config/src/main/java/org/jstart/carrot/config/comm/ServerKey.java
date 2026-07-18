package org.jstart.carrot.config.comm;

public record ServerKey(String namespace, String group,String dataId ) {
    public ServerKey {
        namespace = namespace == null ? "" : namespace;
        group = group == null ? "" : group;
        dataId = dataId == null ? "" : dataId;
    }
}
