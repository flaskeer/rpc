package com.asterisk.rpc.akka;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class Main {

    public static void main(String[] args) {
        akka.Main.main(new String[]{MyUnTypedActor.class.getName()});
    }

}
