package cn.ccc212.utils;

//Base64编码
public class Base64Util {
    private static final char PADCHAR = '=';
    private static final String ALPHA = "LVoJPiCN2R8G90yg+hmFHuacZ1OWMnrsSTXkYpUq/3dlbfKwv6xztjI7DeBE45QA";

    public static String getBase64(String s) {
        StringBuilder result = new StringBuilder();
        int imax = s.length() - s.length() % 3;

        if (s.isEmpty()) {
            return s;
        }

        for (int i = 0; i < imax; i += 3) {
            int b10 = (s.charAt(i) << 16) | (s.charAt(i + 1) << 8) | s.charAt(i + 2);
            result.append(ALPHA.charAt(b10 >> 18))
                  .append(ALPHA.charAt((b10 >> 12) & 63))
                  .append(ALPHA.charAt((b10 >> 6) & 63))
                  .append(ALPHA.charAt(b10 & 63));
        }

        if (s.length() - imax == 1) {
            int b10 = s.charAt(imax) << 16;
            result.append(ALPHA.charAt(b10 >> 18))
                  .append(ALPHA.charAt((b10 >> 12) & 63))
                  .append(PADCHAR).append(PADCHAR);
        } else if (s.length() - imax == 2) {
            int b10 = (s.charAt(imax) << 16) | (s.charAt(imax + 1) << 8);
            result.append(ALPHA.charAt(b10 >> 18))
                  .append(ALPHA.charAt((b10 >> 12) & 63))
                  .append(ALPHA.charAt((b10 >> 6) & 63))
                  .append(PADCHAR);
        }

        return result.toString();
    }
}
