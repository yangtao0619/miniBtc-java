package cn.yangtaotech.utils;


//转换数组和整型的工具类
public class NumberUtil {
    /**
     * int整数转换为4字节的byte数组
     *
     * @param i 整数
     * @return byte数组
     */
    public static byte[] intToByte4(int i) {
        byte[] targets = new byte[4];
        targets[3] = (byte) (i & 0xFF);
        targets[2] = (byte) (i >> 8 & 0xFF);
        targets[1] = (byte) (i >> 16 & 0xFF);
        targets[0] = (byte) (i >> 24 & 0xFF);
        return targets;
    }

    /**
     * long整数转换为8字节的byte数组
     *
     * @param lo long整数
     * @return byte数组
     */
    public static byte[] longToByte8(long lo) {
        byte[] targets = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((lo >>> offset) & 0xFF);
        }
        return targets;
    }

    /**
     * short整数转换为2字节的byte数组
     *
     * @param s short整数
     * @return byte数组
     */
    public static byte[] unsignedShortToByte2(int s) {
        byte[] targets = new byte[2];
        targets[0] = (byte) (s >> 8 & 0xFF);
        targets[1] = (byte) (s & 0xFF);
        return targets;
    }

    /**
     * byte数组转换为无符号short整数
     *
     * @param bytes byte数组
     * @return short整数
     */
    public static int byte2ToUnsignedShort(byte[] bytes) {
        return byte2ToUnsignedShort(bytes, 0);
    }

    /**
     * byte数组转换为无符号short整数
     *
     * @param bytes byte数组
     * @param off   开始位置
     * @return short整数
     */
    public static int byte2ToUnsignedShort(byte[] bytes, int off) {
        int high = bytes[off];
        int low = bytes[off + 1];
        return (high << 8 & 0xFF00) | (low & 0xFF);
    }

    /**
     * byte数组转换为int整数
     *
     * @param bytes byte数组
     * @param off   开始位置
     * @return int整数
     */
    public static int byte4ToInt(byte[] bytes, int off) {
        int b0 = bytes[off] & 0xFF;
        int b1 = bytes[off + 1] & 0xFF;
        int b2 = bytes[off + 2] & 0xFF;
        int b3 = bytes[off + 3] & 0xFF;
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    /**
     * 浮点转换为字节
     *
     * @param f
     * @return
     */
    public static byte[] float2bytes(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }
        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;
    }

    /**
     * 字节转换为浮点
     *
     * @param b     字节（至少4个字节）
     * @param index 开始位置
     * @return
     */
    public static float bytes2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);

    }
}