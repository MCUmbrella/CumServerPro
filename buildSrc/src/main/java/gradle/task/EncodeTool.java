package gradle.task;


public class EncodeTool {
    public static byte[] aesEncode(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] * -1);
        }
        return data;
    }
}
