package cn.yangtaotech;

import cn.yangtaotech.transaction.Transaction;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

//区块对象
public class Block implements Serializable {
    //版本号
    int version;
    //前区块的hash
    byte[] prevHash;
    byte[] hash;
    //挖矿难度
    int difficulty;
    //随机数
    long nonce;
    //时间戳
    long timeStamp;
    //默克尔根
    byte[] merkelRoot;

    //交易的数据
    Transaction[] transactions;

    private static final int difficultyNumber = 16;

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss SSS a");
        Date date = new Date(timeStamp);
        return "version=" + version + "\n" +
                "prevHash=" + new String(prevHash) + "\n" +
                "hash=" + new String(hash) + "\n" +
                "difficulty=" + difficulty + "\n" +
                "nonce=" + nonce + "\n" +
                "timeStamp=" + format.format(date) + "\n" +
                "merkelRoot=" + new String(merkelRoot) + "\n" +
                "transactions'" + Arrays.toString(transactions) + '\'';
    }

    public static Block NewBlock(Transaction[] txs, byte[] prevHash) {
        //拼接字段,返回创建的区块
        Block block = new Block();
        block.version = 11;
        block.prevHash = prevHash;
        block.difficulty = difficultyNumber;
        block.timeStamp = System.currentTimeMillis();
        //计算得到默克尔根
        block.transactions = txs;
        block.merkelRoot = getMerkelRoot(block);
        //添加区块的时候需要进行pow运算
        //首先要创建一个pow对象,让他进行运算,然后返回随机数和运算得到的hash
        ProofOfWork pow = new ProofOfWork(block);
        PowResult powResult = pow.run();
        block.hash = powResult.hash;
        block.nonce = powResult.nonce;
        return block;
    }

    public static Block createGenesisBlock(Transaction[] txs, byte[] prevHash) {
        return NewBlock(txs, prevHash);
    }

    private static byte[] getMerkelRoot(Block block) {
        //这里拼接默克尔根,这里需要遍历区块的每一笔交易,然后将每笔交易的ID拼接做hash运算
        //首先要创建一个长度足够长的字节数组
        int amountLength = 0;
        for (int i = 0; i < block.transactions.length; i++) {
            amountLength += block.transactions[i].id.length;
        }
        byte[] append = new byte[amountLength];
        int countLength = 0;
        for (int i = 0; i < block.transactions.length; i++) {
            byte[] id = block.transactions[i].id;
            System.arraycopy(id, 0, append, countLength, id.length);
            countLength += id.length;
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(append);
            MessageDigest tc1 = (MessageDigest) md.clone();
            return tc1.digest();
        } catch (NoSuchAlgorithmException | CloneNotSupportedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("getMerkelRoot failed!");
    }
}


