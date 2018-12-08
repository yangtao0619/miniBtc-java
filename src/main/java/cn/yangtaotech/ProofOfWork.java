package cn.yangtaotech;

import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;

/**
 * 工作量证明机制,只有当运算完成返回hash和nonce的时候,区块才能创建完成
 */
class ProofOfWork {

    //需要进行pow计算的区块对象
    Block block;

    BigInteger target;

    public ProofOfWork(Block block) {
        this.block = block;
        this.target = BigInteger.valueOf(1).shiftLeft((256 - block.difficulty));
    }

    public PowResult run() {
        System.out.printf("targetValue is %s\n", target.toString(16));

        //根据传入的block和难度值,疯狂的做hash运算,直到找到满足条件的hash,返回拼接的对象
        long nonce = 0;
        //首先要开一个for循环
        for (; true; ) {
            //组装要hash运算的数据
            byte[] mergeResult = prepareData(nonce);
            //使用hash算法得到一个hash并和目标值做比对,满足条件的话直接返回
            String sha256Hex = DigestUtils.sha256Hex(mergeResult);
            if (new BigInteger(sha256Hex, 16).compareTo(target) < 0) {
                System.out.printf("finish pow,nonce is %d,hash is %s \n", nonce, new String(sha256Hex.getBytes()));
                return new PowResult(sha256Hex.getBytes(), nonce);
            } else {
                nonce++;
            }
        }
    }

    /**
     * 合并区块的数据,返回字节数组
     *
     * @param nonce
     * @return
     */
    private byte[] prepareData(long nonce) {
        //将block中的数据拼接成byte数组
        byte[] version = ByteUtils.toBytes(block.version);
        byte[] difficulty = ByteUtils.toBytes(block.difficulty);
        byte[] currentNonce = ByteUtils.toBytes(nonce);
        byte[] timeStamp = ByteUtils.toBytes(block.timeStamp);
        return ByteUtils.merge(version, difficulty, currentNonce, timeStamp, block.prevHash, block.merkelRoot);
    }

    /**
     * 校验函数,检测区块的工作量证明是否有效
     */
    public boolean isValid() {
        byte[] prepareData = prepareData(block.nonce);
        String sha256Hex = DigestUtils.sha256Hex(prepareData);
        return new BigInteger(sha256Hex, 16).compareTo(target) < 0;
    }

}
