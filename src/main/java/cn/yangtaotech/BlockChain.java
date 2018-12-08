package cn.yangtaotech;

import cn.yangtaotech.transaction.Transaction;
import cn.yangtaotech.transaction.TxInput;
import cn.yangtaotech.transaction.TxOutput;
import cn.yangtaotech.transaction.UtxoInfo;
import cn.yangtaotech.utils.SerializebleUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class BlockChain {
    private static final float REWARD = 12.5f;
    //测试的时候将数据写在内存里面
    //正式环境下要将数据写入数据库,所以区块链需要有一个数据库操作的句柄,这个数据还要存储最后一个区块的hash
    public byte[] tailHash;

    private static final String DB_NAME = "blockchain";
    private static final String LAST_BLOCK_HASHNAME = "lastblockhash";

    //添加区块的方法
    public void addBlock(Transaction[] txs, BlockChain bc) {
        File dbFile = new File(DB_NAME);
        if (!dbFile.exists() || !dbFile.isDirectory()) {
            System.out.println("BlockChain not exist,please create first!");
            return;
        }

        Block block = Block.NewBlock(txs, tailHash);
        for (Transaction tx : txs) {
            System.out.println("write block to chain ,tx outputs length is " + tx.outputs.length);
        }
        //序列化创世区块
        byte[] blockBytes = null;
        blockBytes = SerializebleUtil.objectToBytes(block);
        DB db = null;
        db = getLevelDb();
        db.put(block.hash, blockBytes);
        db.put(LAST_BLOCK_HASHNAME.getBytes(), block.hash);
        try {
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("add block end!");
        bc.tailHash = block.hash;
    }

    /**
     * 当区块链数据库不存在的时候,新建一个
     *
     * @return 创建好的区块链
     */
    public static BlockChain NewBlockChain(String address) {
        //新建区块链的时候要创建一个创世区块,在这之前要创建一笔挖矿交易
        //在创建之前,先判断数据库文件是否存在
        File dbFile = new File(DB_NAME);
        if (dbFile.exists() && dbFile.isDirectory()) {
            System.out.println("BlockChain already exist,can not create again!");
            return null;
        }

        BlockChain blockChain = new BlockChain();
        //创建一个创世区块,并创建一笔CoinBase交易打包进这个区块
        Transaction coinBase = createCoinbase(address, "hello bitcoin");
        Block genesisBlock = Block.createGenesisBlock(new Transaction[]{coinBase}, new byte[]{});
        blockChain.tailHash = genesisBlock.hash;
        //创建区块链的时候,需要新建一个数据库,并将第一个区块写入区块链中
        DB db = getLevelDb();
        //序列化创世区块
        byte[] blockBytes = SerializebleUtil.objectToBytes(genesisBlock);
        db.put(genesisBlock.hash, blockBytes);
        db.put(LAST_BLOCK_HASHNAME.getBytes(), genesisBlock.hash);
        try {
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blockChain;
    }

    /**
     * 创建挖矿交易
     *
     * @param miner
     * @param msg
     * @return
     */
    public static Transaction createCoinbase(String miner, String msg) {
        Transaction coinBase = new Transaction();
        //组装交易输入和输出
        TxInput input = new TxInput(null, -1, msg);
        TxOutput output = new TxOutput(REWARD, miner);
        coinBase.inputs = new TxInput[]{input};
        coinBase.outputs = new TxOutput[]{output};
        coinBase.setTxId();
        return coinBase;
    }

    /**
     * 获得一个区块链的对象,如果不存在就提示创建,存在的话直接读取数据库内容返回
     *
     * @return 已存在的区块链对象
     */
    public static BlockChain getBlockChainObject() {
        //先判断数据库文件夹是否存在,不存在返回空并提示先创建区块链
        File dbFile = new File(DB_NAME);
        if (!dbFile.exists() || !dbFile.isDirectory()) {
            System.out.println("BlockChain not exist,please create first!");
            return null;
        }
        //存在的话就重新读取数据
        //获取leveldb数据库实例
        DB levelDb = null;
        levelDb = getLevelDb();
        BlockChain blockChain = new BlockChain();
        assert levelDb != null;
        byte[] tailHash = levelDb.get(LAST_BLOCK_HASHNAME.getBytes());
        try {
            levelDb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        blockChain.tailHash = tailHash;
        return blockChain;
    }

    /**
     * 创建用于存储区块的数据库句柄
     */
    public static DB getLevelDb() {
        Options options = new Options();
        options.createIfMissing(true);
        DB db = null;
        try {
            db = factory.open(new File(DB_NAME), options);
        } catch (IOException e) {
            System.out.println("get leveldb fail!");
            e.printStackTrace();
        }
        return db;
    }

    /**
     * 找到当前区块链中所有UTXO
     */
    public List<UtxoInfo> findAllUtxos(String sendAddress) {
        //遍历所有的区块,找出交易发起人能够解锁的所有没有花费的交易输出
        List<UtxoInfo> utxoInfos = new ArrayList<>();
        Iterator blockIterator = new Iterator(tailHash);

        //byte[]不能直接作为map的key使用,所以需要将txid转换成string才能使用
        Map<String, List<Integer>> spentOutputs = new HashMap<>();
        for (; ; ) {
            Block block = blockIterator.getBlock();

            for (Transaction tx : block.transactions) {
                System.out.println("tag2:outputs length is " + tx.outputs.length + " tx id is " + new BigInteger(tx.id).toString(16));
                ScanTransaction:
                //遍历所有的交易输出
                for (int i = 0; i < tx.outputs.length; i++) {
                    //如果发现该交易已经有被引用的交易输出,就开始判断
                    List<Integer> idList = spentOutputs.get(new String(tx.id));
                    if (idList != null) {
                        System.out.println(idList.toString());
                        for (Integer index : idList) {
                            if (i == index) {
                                System.out.println("find spent output,continue scan next input");
                                continue ScanTransaction;
                            }
                        }
                    }
                    //如果交易发起人能使用,将其添加进去
                    TxOutput output = tx.outputs[i];
                    if (output.canUnlockUTXOWith(sendAddress)) {
                        UtxoInfo utxoInfo = new UtxoInfo();
                        utxoInfo.id = tx.id;
                        utxoInfo.index = i;
                        utxoInfo.output = output;
                        utxoInfos.add(utxoInfo);
                    }
                }

                //遍历所有的交易输入,找到已经花费掉的,每次循环都创建一个List对象
                List<Integer> spentTxIds = new ArrayList<>();
                if (!tx.isCoinBase()) {
                    for (int i = 0; i < tx.inputs.length; i++) {
                        TxInput input = tx.inputs[i];
                        if (input.canUnlockWith(sendAddress)) {
                            spentTxIds.add(i);
                            //一定要注意这里的txid是输入中引用的交易id,不是本次循环的txid
                            System.out.println("find used utxo,tx id is " + new BigInteger(input.txId).toString(16) + "index is " + i);
                            spentOutputs.put(new String(input.txId), spentTxIds);
                        }
                    }
                }
            }

            if (block.prevHash == null || block.prevHash.length == 0) {
                break;
            }
        }
        return utxoInfos;
    }

    /**
     * 找到合适的UTXO
     */
    public SuitableUtxoResult getSuitableUtxoInfos(String address, float value) {
        //得到这个地址能解锁的所有UTXO
        List<UtxoInfo> allUtxos = findAllUtxos(address);
        float add = 0.0f;
        Map<byte[], List<Integer>> map = new HashMap<>();
        for (UtxoInfo info : allUtxos) {
            byte[] key = info.id;
            //判断这个key是否存在,存在话就取出原来的值添加一个
            if (map.containsKey(key)) {
                map.get(key).add(info.index);
            } else {
                List<Integer> list = new ArrayList<>();
                list.add(info.index);
                map.put(key, list);
            }
            add += info.output.value;
            if (add >= value) {
                break;
            }
        }
        System.out.println("find suitable utxoinfo,map is " + map.size());
        return new SuitableUtxoResult(map, add);
    }

    /**
     * 使用按照插入序排序的迭代器
     */
    public void printAllBlockInfoByNewIterator() {
        System.out.println("-----------------------------------------start print all blocks------------------------------------------");
        Iterator iterator = new Iterator(tailHash);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss SSS a");
        for (; ; ) {
            Block block = iterator.getBlock();
            System.out.println("version is : " + block.version);
            System.out.printf("prevHash is : %s\n", new String(block.prevHash));
            System.out.printf("hash is : %s\n", new String(block.hash));
            System.out.println("nonce is : " + block.nonce);
            System.out.println("difficulty is : " + block.difficulty);
            //校验工作量证明是否成功
            ProofOfWork proofOfWork = new ProofOfWork(block);
            System.out.println("pow is valid : " + proofOfWork.isValid());
            //日期的格式化输出
            Date date = new Date(block.timeStamp);
            System.out.println("timeStamp is : " + format.format(date));
            System.out.printf("merkelRoot is : %x\n", new BigInteger(block.merkelRoot));
            System.out.println("transactions is : " + Arrays.toString(block.transactions));
            System.out.println("-----------------------------------------next block------------------------------------------");
            if (block.prevHash.length == 0) {
                break;
            }
        }
    }

    /**
     * 使用leveldb自带的迭代器打印所有的区块信息
     */
    public void printAllBlockInfo() {
        System.out.println("start print blocks");
        DBIterator iterator = null;
        iterator = getLevelDb().iterator();
        for (; iterator.hasNext(); ) {
            Map.Entry<byte[], byte[]> next = iterator.next();
            byte[] key = next.getKey();
            byte[] value = next.getValue();
            //比较两个字节数组
            if (Arrays.equals(key, LAST_BLOCK_HASHNAME.getBytes())) break;
            Block block = SerializebleUtil.bytesToBlock(value);
            System.out.println(block.toString());
            System.out.println("-----------------------------------------next block------------------------------------------");
        }
    }
}
