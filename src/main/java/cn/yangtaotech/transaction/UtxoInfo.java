package cn.yangtaotech.transaction;

import java.util.Arrays;

/**
 * 未花费的交易输出
 */
public class UtxoInfo {
    //未花费的交易输出应该包含所在交易的id,index以及该Output对象
    public byte[] id;
    public int index;
    public TxOutput output;

    @Override
    public String toString() {
        return "UtxoInfo{" +
                "id=" + Arrays.toString(id) +
                ", index=" + index +
                ", output=" + output +
                "}\n";
    }
}
