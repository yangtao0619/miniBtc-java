package cn.yangtaotech.transaction;

import java.io.Serializable;
import java.math.BigInteger;

public class TxInput implements Serializable {
    //交易输入包含解锁脚本,引用的output所在的交易id,引用的output所在交易中的index,签名
    public byte[] txId;

    int index;

    //交易输入中包含解锁脚本,先用接收人的姓名表示
    String signScript;

    public TxInput(byte[] txId, int index, String signScript) {
        this.txId = txId;
        this.index = index;
        this.signScript = signScript;
    }

    /**
     * 验证这个UTXO能不能使用这个地址解锁
     *
     * @return
     */
    public boolean canUnlockWith(String toAddress) {
        return signScript.equals(toAddress);
    }

    @Override
    public String toString() {
        return "TxInput{" +
                "txId=" + (txId == null ? " " : new BigInteger(txId).toString(16)) +
                ", index=" + index +
                ", signScript='" + signScript + '\'' +
                "}\n";
    }
}
