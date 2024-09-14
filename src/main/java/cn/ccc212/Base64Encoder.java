package cn.ccc212;

//Base64编码
public class Base64Encoder {
    private static final char PADCHAR = '=';
    private static final String ALPHA = "LVoJPiCN2R8G90yg+hmFHuacZ1OWMnrsSTXkYpUq/3dlbfKwv6xztjI7DeBE45QA";

    private static int getByte(String s, int i) {
        int x = (int) s.charAt(i);
        if (x > 255) {
            throw new IllegalArgumentException("INVALID_CHARACTER_ERR: DOM Exception 5");
        }
        return x;
    }

    public static String getBase64(String s) {
        StringBuilder result = new StringBuilder();
        int imax = s.length() - s.length() % 3;

        if (s.length() == 0) {
            return s;
        }

        for (int i = 0; i < imax; i += 3) {
            int b10 = (getByte(s, i) << 16) | (getByte(s, i + 1) << 8) | getByte(s, i + 2);
            result.append(ALPHA.charAt(b10 >> 18))
                  .append(ALPHA.charAt((b10 >> 12) & 63))
                  .append(ALPHA.charAt((b10 >> 6) & 63))
                  .append(ALPHA.charAt(b10 & 63));
        }

        int i = imax;
        if (s.length() - imax == 1) {
            int b10 = getByte(s, i) << 16;
            result.append(ALPHA.charAt(b10 >> 18))
                  .append(ALPHA.charAt((b10 >> 12) & 63))
                  .append(PADCHAR).append(PADCHAR);
        } else if (s.length() - imax == 2) {
            int b10 = (getByte(s, i) << 16) | (getByte(s, i + 1) << 8);
            result.append(ALPHA.charAt(b10 >> 18))
                  .append(ALPHA.charAt((b10 >> 12) & 63))
                  .append(ALPHA.charAt((b10 >> 6) & 63))
                  .append(PADCHAR);
        }

        return result.toString();
    }
}
