package cn.yangtaotech.transaction;

import java.io.Serializable;

public class TxOutput implements Serializable {
    //交易输出包含价值和加密脚本
    public float value;

    //交易输出包含加密脚本,加密脚本可以使用付款人的地址先代替
    String pubkeyString;

    public TxOutput(float value, String pubkeyString) {
        this.value = value;
        this.pubkeyString = pubkeyString;
    }

    //验证这个utxo是不是这个地址创建的
    public boolean canUnlockUTXOWith(String from) {
        return pubkeyString.equals(from);
    }

    @Override
    public String toString() {
        return "TxOutput{" +
                "value=" + value +
                ", pubkeyString='" + pubkeyString + '\'' +
                "}\n";
    }
}
