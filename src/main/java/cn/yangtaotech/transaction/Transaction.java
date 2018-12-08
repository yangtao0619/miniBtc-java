package cn.yangtaotech.transaction;

import cn.yangtaotech.BlockChain;
import cn.yangtaotech.SuitableUtxoResult;
import cn.yangtaotech.utils.SerializebleUtil;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

//定义交易的结构
public class Transaction implements Serializable {
    public byte[] id;//交易的id
    public TxInput[] inputs;
    public TxOutput[] outputs;

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + new BigInteger(id).toString(16) +
                ", inputs=" + Arrays.toString(inputs) +
                ", outputs=" + Arrays.toString(outputs) +
                "}\n";
    }

    /**
     * 给交易设置id
     */
    public void setTxId() {
        //首先是给交易对象编码成字节流
        try {
            byte[] objectToBytes = SerializebleUtil.objectToBytes(this);
            //对得到的字节数组进行sha256计算
            MessageDigest md = null;
            //使用数字摘要算法SHA256进行散列,具体的名称列表可见
            // https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
            md = MessageDigest.getInstance("SHA-256");
            md.update(objectToBytes);
            MessageDigest tc1 = (MessageDigest) md.clone();
            //将字节流设置给id
            this.id = tc1.digest();
        } catch (NoSuchAlgorithmException | CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static Transaction createNewTransaction(String from, String to, float value) {
        //首先应该找到满足value条件的utxo
        BlockChain blockChain = BlockChain.getBlockChainObject();
        assert blockChain != null;
        SuitableUtxoResult utxoInfos = blockChain.getSuitableUtxoInfos(from, value);
        Iterator<Map.Entry<byte[], List<Integer>>> iterator = utxoInfos.utxoInfo.entrySet().iterator();
        List<TxInput> inputs = new ArrayList<>();
        List<TxOutput> outputs = new ArrayList<>();
        //组装输入
        while (iterator.hasNext()) {
            Map.Entry<byte[], List<Integer>> next = iterator.next();
            byte[] key = next.getKey();
            List<Integer> list = next.getValue();
            System.out.println("create input list size is " + list.size());
            for (Integer index : list) {
                TxInput input = new TxInput(key, index, from);
                inputs.add(input);
            }
        }

        //组装输出,一笔交易发给收款人
        outputs.add(new TxOutput(value, to));

        if (utxoInfos.amount > value) {
            //创建一笔交易给自己
            System.out.println("create output to from ,value :" + (utxoInfos.amount - value));
            outputs.add(new TxOutput(utxoInfos.amount - value, from));
        }
        Transaction transaction = new Transaction();

        TxInput[] newInputs = new TxInput[inputs.size()];
        for (int i = 0; i < newInputs.length; i++) {
            newInputs[i] = inputs.get(i);
        }
        TxOutput[] newOutputs = new TxOutput[outputs.size()];
        for (int i = 0; i < newOutputs.length; i++) {
            newOutputs[i] = outputs.get(i);
        }
        transaction.inputs = newInputs;
        transaction.outputs = newOutputs;
        System.out.println("tag1:outputs length is "+transaction.outputs.length+" newOutputs size "+ newOutputs.length);
        transaction.setTxId();
        return transaction;
    }

    /**
     * 是否是挖矿交易
     *
     * @return
     */
    public boolean isCoinBase() {
        if (inputs.length == 1) {
            if (inputs[0].index == -1 && inputs[0].txId == null) {
                return true;
            }
        }
        return false;
    }
}


