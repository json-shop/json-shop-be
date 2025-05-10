package deepdive.jsonstore.common.util;

import de.huxhorn.sulky.ulid.ULID;

public class UlidUtil {

    private static final ULID ULID = new ULID();

    public static byte[] createUlidBytes() {
        ULID.Value value = ULID.nextValue();
        return value.toBytes();
    }

    public static ULID.Value convertToUlidValueFrom(byte[] ulidBytes) {
        return ULID.fromBytes(ulidBytes);
    }

    public static String getUlidString(byte[] ulidBytes) {
        ULID.Value value = convertToUlidValueFrom(ulidBytes);
        return value.toString();
    }
}
