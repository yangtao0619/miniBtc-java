package cn.yangtaotech;

import java.util.List;
import java.util.Map;

//找到的所有合适的UTXOinfo
public class SuitableUtxoResult {
    public Map<byte[], List<Integer>> utxoInfo;
    public float amount;

    public SuitableUtxoResult(Map<byte[], List<Integer>> utxoInfo, float amount) {
        this.utxoInfo = utxoInfo;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "SuitableUtxoResult{" +
                "utxoInfo=" + utxoInfo +
                ", amount=" + amount +
                '}';
    }
}
