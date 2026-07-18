package org.jstart.carrot.dubbo.registry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Dubbo `carrot://` Registry SPI 工厂。
 */
public class CarrotDubboRegistryFactory implements RegistryFactory {
    private final ConcurrentMap<String, Registry> registries = new ConcurrentHashMap<>();

    @Override
    public Registry getRegistry(URL url) {
        Objects.requireNonNull(url, "url");
        return registries.computeIfAbsent(buildRegistryKey(url), key -> new CarrotDubboRegistry(url));
    }

    private String buildRegistryKey(URL url) {
        String namespace = url.getParameter("namespace", "");
        String namespaceB64 = url.getParameter("carrotNamespaceB64", "");
        String accessToken = url.getParameter("accessToken", "");
        String backup = url.getParameter("backup", "");
        return url.getProtocol() + "://" + url.getAddress()
                + "?namespace=" + namespace
                + "&carrotNamespaceB64=" + namespaceB64
                + "&accessToken=" + accessToken
                + "&backup=" + backup;
    }
}
