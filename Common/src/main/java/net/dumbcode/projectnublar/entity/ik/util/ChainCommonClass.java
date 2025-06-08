package net.dumbcode.projectnublar.entity.ik.util;

import java.util.logging.Logger;

public class ChainCommonClass {
    public static Logger LOGGER = Logger.getLogger("Chain");
    public static boolean isDev = true;
    public static boolean shouldRenderDebugLegs = false;

    public static void throwInDevOnly(RuntimeException exception) {
        if (isDev) {
            throw exception;
        }
    }

    public static void init() {

    }
}
