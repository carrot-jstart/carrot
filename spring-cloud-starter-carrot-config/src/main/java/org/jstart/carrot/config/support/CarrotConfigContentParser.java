package org.jstart.carrot.config.support;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置内容解析器。
 * <p>
 * 将配置中心返回的文本内容（如 YAML / properties）解析为扁平化的 {@code key -> value} 结构，
 * 以便写入 {@link org.springframework.core.env.PropertySource}。
 * </p>
 */
public final class CarrotConfigContentParser {
    private CarrotConfigContentParser() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将指定配置文件内容解析为可写入 Environment 的属性 Map。
     *
     * @param dataId      配置文件标识（通常包含扩展名，例如 application.yml）
     * @param content     配置文本内容
     * @param contentType 内容类型（例如 text/yaml、text/plain 等）
     * @return 扁平化后的属性 Map
     */
    public static Map<String, Object> parseToProperties(String dataId, String content, String contentType) {
        String did = dataId == null ? "" : dataId.trim().toLowerCase();
        String ct = contentType == null ? "" : contentType.trim().toLowerCase();
        boolean yaml = did.endsWith(".yml") || did.endsWith(".yaml") || ct.contains("yaml") || ct.contains("yml");
        if (yaml) {
            return parseYamlToProperties(content);
        }
        boolean properties = did.endsWith(".properties") || ct.contains("properties");
        if (properties) {
            return parsePropertiesToMap(content);
        }
        return parsePropertiesToMap(content);
    }

    /**
     * 将 YAML 文本解析为扁平化属性。
     * <p>
     * 仅处理简单的 {@code key: value} 以及缩进层级；数组/列表（以 {@code -} 开头）会被忽略。
     * </p>
     *
     * @param yamlText YAML 文本
     * @return 扁平化后的属性 Map
     */
    private static Map<String, Object> parseYamlToProperties(String yamlText) {
        if (yamlText == null || yamlText.isBlank()) {
            return Map.of();
        }
        Map<String, Object> out = new HashMap<>();
        Deque<Node> stack = new ArrayDeque<>();

        for (String rawLine : yamlText.split("\\r?\\n")) {
            if (rawLine == null) {
                continue;
            }
            String line = stripComment(rawLine);
            if (line.isBlank()) {
                continue;
            }
            int indent = countLeadingSpaces(line);
            if (indent == 1) {
                indent = 0;
                stack.clear();
            }
            String trimmed = line.trim();
            if (trimmed.startsWith("-")) {
                continue;
            }

            int colon = trimmed.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String key = trimmed.substring(0, colon).trim();
            String after = trimmed.substring(colon + 1).trim();

            while (!stack.isEmpty() && indent <= stack.peekLast().indent) {
                stack.removeLast();
            }

            if (after.isEmpty()) {
                stack.addLast(new Node(indent, key));
                continue;
            }

            String fullKey = join(stack, key);
            out.put(fullKey, unquote(after));
        }
        return out;
    }

    /**
     * 将当前缩进栈与叶子节点拼接为点分隔的完整 key。
     *
     * @param stack 缩进层级栈
     * @param leaf  当前行的 key
     * @return 完整属性 key
     */
    private static String join(Deque<Node> stack, String leaf) {
        if (stack.isEmpty()) {
            return leaf;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Node n : stack) {
            if (!first) {
                sb.append('.');
            }
            sb.append(n.key);
            first = false;
        }
        sb.append('.').append(leaf);
        return sb.toString();
    }

    /**
     * 将 properties 格式文本解析为 Map。
     * <p>
     * 支持 {@code key=value} 与 {@code key: value} 两种分隔符；忽略空行与以 {@code #}/{@code !} 开头的注释行。
     * </p>
     *
     * @param text properties 文本
     * @return 属性 Map
     */
    private static Map<String, Object> parsePropertiesToMap(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        Map<String, Object> out = new HashMap<>();
        for (String raw : text.split("\\r?\\n")) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                continue;
            }
            int idx = line.indexOf('=');
            int idx2 = line.indexOf(':');
            int sep;
            if (idx < 0) {
                sep = idx2;
            } else if (idx2 < 0) {
                sep = idx;
            } else {
                sep = Math.min(idx, idx2);
            }
            if (sep <= 0) {
                continue;
            }
            String key = line.substring(0, sep).trim();
            String value = line.substring(sep + 1).trim();
            if (!key.isEmpty()) {
                out.put(key, value);
            }
        }
        return out;
    }

    /**
     * 去除 YAML 行内注释（以 {@code #} 开始）。
     *
     * @param line 原始行
     * @return 去除注释后的字符串
     */
    private static String stripComment(String line) {
        String s = line;
        int idx = s.indexOf('#');
        if (idx >= 0) {
            s = s.substring(0, idx);
        }
        return s;
    }

    /**
     * 统计行首空格数量，用于计算 YAML 缩进级别。
     *
     * @param s 行文本
     * @return 行首空格数量
     */
    private static int countLeadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    /**
     * 去除首尾成对的单引号或双引号。
     *
     * @param s 原始字符串
     * @return 去引号后的字符串
     */
    private static String unquote(String s) {
        String v = s.trim();
        if (v.length() >= 2) {
            char a = v.charAt(0);
            char b = v.charAt(v.length() - 1);
            if ((a == '"' && b == '"') || (a == '\'' && b == '\'')) {
                return v.substring(1, v.length() - 1);
            }
        }
        return v;
    }

    private record Node(int indent, String key) {
    }
}
