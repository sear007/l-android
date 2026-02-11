package co.ltlabs.ltmechanic.util;

public class StrUtil {
    public static String replaceStr(String str) {
        return str.replaceAll("\\[", "%s").replaceAll("]", "");
    }
}