package cn.yangtaotech;

import cn.yangtaotech.transaction.Transaction;
import cn.yangtaotech.transaction.TxInput;
import cn.yangtaotech.transaction.UtxoInfo;
import cn.yangtaotech.wallet.Wallet;
import cn.yangtaotech.wallet.WalletUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

public class Client {
    BlockChain blockChain;

    private static final String USAGE =
            "createblockchain | create a new blockchain\n" +
                    "addblock [data] | add new block to chain\n" +
                    "printbc | print all blocks\n" +
                    "createNewWallet | create a new wallet and address\n" +
                    "send from [account] to [account] miner [account] [value] [data] | send transaction" +
                    "getBalance [address] | get balance of address" +
                    "getAllAddresses | get all addresses";

    public void run() throws IOException {
        for (; true; ) {
            System.out.println("please input command:");
            //接收命令行输入的命令,这种方式接收可以避免空格之后的内容不被接收
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String cmd = br.readLine();
            if (cmd == null || "".equals(cmd)) {
                return;
            }
            String[] args = cmd.split("\\s+");
            if (args.length >= 1) {
                switch (args[0]) {
                    case "createblockchain":
                        if (!checkNum(args, 2)) {
                            System.out.println("args error!");
                            break;
                        }
                        //使用地址生成一条区块链,创世区块中的coinbase指向这个地址
                        blockChain = BlockChain.NewBlockChain(args[1]);
                        break;
                    case "addblock":
                    /*    BlockChain bc1 = BlockChain.getBlockChainObject();
                        if (!checkNum(args, 2)) {
                            System.out.println("args error!");
                            break;
                        } else if (bc1 == null) {
                            System.out.println("blockChain is null,please create first!");
                            break;
                        }
                        //向区块链中添加新数据
                        String data = args[1];
                        bc1.addBlock(data, bc1);*/
                        break;
                    case "printbc":
                        BlockChain bc = BlockChain.getBlockChainObject();
                        if (!checkNum(args, 1)) {
                            System.out.println("args error!");
                            break;
                        } else if (bc == null) {
                            System.out.println("blockChain is null,please create first!");
                            break;
                        }
                        //打印区块链数据
                        bc.printAllBlockInfoByNewIterator();
                        break;
                    case "send":
                        BlockChain bc2 = BlockChain.getBlockChainObject();
                        if (!checkNum(args, 9)) {
                            System.out.println("args error!");
                            break;
                        } else if (bc2 == null) {
                            System.out.println("blockChain is null,please create first!");
                            break;
                        }
                        //创建一笔交易 send from [account] to [account] miner [account] [value] [data] | send transaction
                        String from = args[2];
                        String to = args[4];
                        String miner = args[6];
                        float value = Float.parseFloat(args[7]);
                        String msg = args[8];
                        sendMoneyToSomeone(from, to, value, miner, msg, bc2);
                        break;
                    case "createNewWallet":
                        if (!checkNum(args, 1)) {
                            System.out.println("args error!");
                            break;
                        }
                        //创建一个新的钱包
                        Wallet wallet = WalletUtils.getInstance().createWallet();
                        String address = wallet.getAddress();
                        System.out.println("create new wallet success! address is " + address);
                        break;
                    case "getBalance":
                        if (!checkNum(args, 2)) {
                            System.out.println("args error!");
                            break;
                        }
                        //得到某个地址的余额
                        float balance = Wallet.getBalance(args[1]);
                        System.out.println("balance of " + args[1] + " is " + balance);
                        break;
                    case "getAllAddresses":
                        //得到所有的钱包地址
                        if (!checkNum(args, 1)) {
                            System.out.println("args error!");
                            break;
                        }
                        Set<String> addresses = WalletUtils.getInstance().getAddresses();
                        System.out.println("all addresses list:");
                        assert addresses != null;
                        for (String addr : addresses) {
                            System.out.println(addr);
                        }
                        break;
                    default:
                        System.out.println(USAGE);
                }
            }
        }
    }

    //发起转账交易的方法
    private void sendMoneyToSomeone(String from, String to, float value, String miner, String msg, BlockChain bc) {
        //这里需要组装完成一笔交易,然后添加到新的区块中
        //需要将这笔交易添加到区块中打包,区块应该包含两笔交易,一笔是挖矿交易,一笔是发起的交易
        Transaction coinbase = BlockChain.createCoinbase(miner, msg);
        coinbase.setTxId();
        //创建一笔普通交易
        Transaction tx = Transaction.createNewTransaction(from, to, value);
        //添加到区块链中
        System.out.println("tag1: length " + tx.outputs.length);
        bc.addBlock(new Transaction[]{coinbase, tx}, bc);
    }

    private boolean checkNum(String[] args, int num) {
        return args.length == num;
    }
}
