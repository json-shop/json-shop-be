package deepdive.jsonstore.common.util;

import de.huxhorn.sulky.ulid.ULID;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UlidUtil {

    private static final ULID ULID = new ULID();

    // ULID 생성
    public static byte[] createUlidBytes() {
        ULID.Value value = ULID.nextValue();
        return value.toBytes();
    }


    public static ULID.Value convertToUlidValueFrom(byte[] ulidBytes) {
        return ULID.fromBytes(ulidBytes);
    }

    // ULID를 String으로 변환
    public static String getUlidString(byte[] ulidBytes) {
        ULID.Value value = convertToUlidValueFrom(ulidBytes);
        return value.toString();
    }


    // ULID를 UUID타입으로 생성
    public static UUID createUlid(){
        // 1. ULID 생성
        ULID.Value ulidValue = ULID.nextValue();
        // 2) 16바이트 배열로 추출
        byte[] ulidBytes = ulidValue.toBytes();
        // 3. 바이트 배열 -> 두 개의 long(msb, lsb)로 분리
        ByteBuffer bb = ByteBuffer.wrap(ulidBytes);
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        // 4. UUID 생성
        UUID ulidAsUuid = new UUID(mostSigBits, leastSigBits);
        return ulidAsUuid;
    }
}
