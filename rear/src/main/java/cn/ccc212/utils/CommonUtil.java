package cn.ccc212.utils;

import org.jsoup.internal.StringUtil;

import java.util.function.Supplier;

public class CommonUtil {

    public static String getValueOrDefault(String input, Supplier<String> defaultValueSupplier) {
        return StringUtil.isBlank(input) ? defaultValueSupplier.get() : input;
    }
}
