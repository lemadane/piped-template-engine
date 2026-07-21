package io.succinct.piped.template.engine.utils;

public final class HtmlFormatter {

    public static String minifyHtml(String html) {
        if (html == null) {
            return "";
        }
        String result = html.replaceAll("<!--[\\s\\S]*?-->", "");
        return result.replaceAll("\\s+", " ")
                     .replaceAll(">\\s+<", "><")
                     .trim();
    }

    public static String prettifyHtml(String html) {
        if (html == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        String[] tokens = html.split("(?<=<)|(?=>)");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("</")) {
                indent = Math.max(0, indent - 1);
                sb.append("\n").append("  ".repeat(indent)).append(trimmed);
            } else if (trimmed.startsWith("<") && !trimmed.startsWith("<!") && !trimmed.endsWith("/>") && !trimmed.startsWith("<?")) {
                sb.append("\n").append("  ".repeat(indent)).append(trimmed);
                indent++;
            } else {
                sb.append(trimmed);
            }
        }
        return sb.toString().trim();
    }
}
