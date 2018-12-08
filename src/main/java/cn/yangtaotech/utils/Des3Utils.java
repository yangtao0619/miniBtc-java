package cn.yangtaotech.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class Des3Utils {

    private static String PASSWORD_CRYPT_KEY;
    private static String Algorithm;
    static {
        InputStream in = Des3Utils.class.getClassLoader()
                .getResourceAsStream("3desconfig.properties");
        Properties pro = new Properties();
        try {
            pro.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PASSWORD_CRYPT_KEY = pro.getProperty("PASSWORD_CRYPT_KEY");
        Algorithm = pro.getProperty("Algorithm");
        System.out.println("PASSWORD_CRYPT_KEY:" + PASSWORD_CRYPT_KEY + ",Algorithm:" + Algorithm);
    }

    /*
     * 根据 密钥字符串生成密钥字节数组
     * @param keyStr 密钥字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
        byte[] key = new byte[24]; // 声明一个24位的字节数组，默认里面都是0
        byte[] temp = keyStr.getBytes("UTF-8"); // 将字符串转成字节数组

        /*
         * 执行数组拷贝 System.arraycopy(源数组，从源数组哪里开始拷贝，目标数组，拷贝多少位)
         */
        if (key.length > temp.length) {
            // 如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, temp.length);
        } else {
            // 如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, key.length);
        }
        return key;
    }

}
