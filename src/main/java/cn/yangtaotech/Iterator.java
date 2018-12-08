package cn.yangtaotech;

import cn.yangtaotech.utils.SerializebleUtil;
import org.iq80.leveldb.DB;

import java.io.IOException;

/*
用于迭代遍历区块链的迭代器
 */
class Iterator {

    //迭代器可以由区块链创建,内部有一个存储当前key的hash
    private byte[] currentHash;

    Iterator(byte[] currentHash) {
        this.currentHash = currentHash;
    }

    /**
     * 从数据库中读取一个区块数据并返回
     * 按照插入顺序的逆序
     */
    Block getBlock() {
        //首先需要打开数据库
        DB levelDb;
        Block block;
        levelDb = BlockChain.getLevelDb();
        //根据currenthash读取区块数据
        byte[] blockHash = levelDb.get(currentHash);
        block = SerializebleUtil.bytesToBlock(blockHash);
        //将currenthash指向前一个区块的hash
        currentHash = block.prevHash;
        try {
            levelDb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return block;
    }
}
