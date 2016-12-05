package uk.co.terminological.javapig.util;

import java.util.Locale;

public class SourceCode {

    /**
     * Shamelessly stolen and modified from Apache commons StringEscapeUtils 
     * https://commons.apache.org/proper/commons-lang/javadocs/api-2.6/src-html/org/apache/commons/lang/StringEscapeUtils.html#line.89
     * http://www.apache.org/licenses/LICENSE-2.0
     * 
     * @param str String to escape values in, may be null
     * @throws IOException if an IOException occurs
     */
    public static String string(String str) {
        if (str == null) {return null;}
        StringBuilder out = new StringBuilder("\"");
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            // handle unicode
            if (ch > 0xfff) {
                out.append("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.append("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.append("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b' :
                        out.append('\\');
                        out.append('b');
                        break;
                    case '\n' :
                        out.append('\\');
                        out.append('n');
                        break;
                    case '\t' :
                        out.append('\\');
                        out.append('t');
                        break;
                    case '\f' :
                        out.append('\\');
                        out.append('f');
                        break;
                    case '\r' :
                        out.append('\\');
                        out.append('r');
                        break;
                    default :
                        if (ch > 0xf) {
                            out.append("\\u00" + hex(ch));
                        } else {
                            out.append("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '"' :
                        out.append('\\');
                        out.append('"');
                        break;
                    case '\\' :
                        out.append('\\');
                        out.append('\\');
                        break;
                    default :
                        out.append(ch);
                        break;
                }
            }
        }
        out.append("\"");
        return out.toString();
    }

    /**
     * <p>Returns an upper case hexadecimal <code>String</code> for the given
     * character.</p>
     * 
     * @param ch The character to convert.
     * @return An upper case hexadecimal <code>String</code>
     */
    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
    
    public static String bool(boolean in) {
    	return in ? "true" : "false";
    }
}