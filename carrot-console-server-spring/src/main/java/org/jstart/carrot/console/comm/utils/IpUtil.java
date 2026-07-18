package org.jstart.carrot.console.comm.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IpUtil {

    public final static String LOCAL_IP = getFirstNonLoopbackIPv4();

    private IpUtil() {
        throw new IllegalStateException("Utility class");
    }
    /**
     * 获取本机第一个非回环 IPv4 地址。
     * 简单场景下使用，会在找到第一个符合条件的地址后立即返回。
     * @return 本机非回环 IPv4 地址，未找到则返回 null
     */
    public static String getFirstNonLoopbackIPv4() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 1. 过滤网卡：跳过回环和未启用的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // 2. 过滤地址：跳过回环地址，只保留 IPv4
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * 获取所有可用的非回环 IPv4 地址。
     * 适用于多网卡服务器场景，允许业务代码自行决定使用哪个 IP。
     * @return 所有符合条件的 IPv4 地址列表，若无则返回空列表
     */
    public static List<String> getAllNonLoopbackIPv4s() {
        List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 过滤掉回环和未启用的网卡
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // 过滤掉回环地址，只保留 IPv4
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        ipList.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return ipList;
        }
        return ipList;
    }
}
