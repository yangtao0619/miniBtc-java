package cn.yangtaotech.utils;

import cn.yangtaotech.Block;
import cn.yangtaotech.transaction.Transaction;

import java.io.*;

//将block进行序列化和反序列化的工具类
public class SerializebleUtil {

    public static byte[] objectToBytes(Object object) {
        //创建内存输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static Block bytesToBlock(byte[] seriaBlock) {
        //反序列化
        Block block = null;
        try {
            //创建内存输入列
            ByteArrayInputStream bais = new ByteArrayInputStream(seriaBlock);
            ObjectInputStream ois = new ObjectInputStream(bais);
            block = (Block) ois.readObject();
            ois.close();
            bais.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return block;
    }

    public static Transaction bytesToTransaction(byte[] seriaTransaction) {
        //反序列化
        Transaction transaction = null;
        try {
            //创建内存输入列
            ByteArrayInputStream bais = new ByteArrayInputStream(seriaTransaction);
            ObjectInputStream ois = new ObjectInputStream(bais);
            transaction = (Transaction) ois.readObject();
            ois.close();
            bais.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return transaction;
    }
}
