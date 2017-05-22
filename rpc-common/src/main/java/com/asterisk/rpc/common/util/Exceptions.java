package com.asterisk.rpc.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class Exceptions {

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return null;
    }

}
