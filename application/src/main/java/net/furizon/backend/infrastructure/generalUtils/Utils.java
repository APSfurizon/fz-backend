package net.furizon.backend.infrastructure.generalUtils;

import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;

public class Utils {

    public static byte[] fromHex(@NotNull String hex) {
        int hexLength = hex.length();
        boolean finalHfound = false;
        boolean start0xFound = false;

        if (hexLength >= 1) {
            char end = hex.charAt(hexLength - 1);
            if (end == 'h' || end == 'H') {
                finalHfound = true;
            }
        }
        if (hexLength >= 2) {
            char start0 = hex.charAt(0);
            char start1 = hex.charAt(1);
            if (start0 == '0' && (start1 == 'x' || start1 == 'X')) {
                start0xFound = true;
            }
        }
        if (finalHfound || start0xFound) {
            hex = hex.substring(start0xFound ? 2 : 0, hexLength - (finalHfound ? 1 : 0));
        }

        return Hex.decode(hex);
    }
}
