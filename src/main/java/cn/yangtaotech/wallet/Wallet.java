package cn.yangtaotech.wallet;

import cn.yangtaotech.BlockChain;
import cn.yangtaotech.transaction.UtxoInfo;
import cn.yangtaotech.utils.Base58Check;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.Arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.util.List;

public class Wallet implements Serializable {

    private BCECPrivateKey privateKey;

    private byte[] publicKey;

    //使用椭圆曲线算法生成非对称加密的密钥对
    Wallet() {
        try {
            KeyPair keyPair = getKeyPair();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

            //对公钥进行编码转换成字节数组
            byte[] pubkeyBytes = publicKey.getQ().getEncoded(false);
            this.privateKey = privateKey;
            this.publicKey = pubkeyBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //生成公钥和私钥密钥对,使用ecdsa算法
    private KeyPair getKeyPair() throws Exception {
        //注册
        Security.addProvider(new BouncyCastleProvider());
        //创建密钥对生成器
        KeyPairGenerator ecdsaKeypairGenerator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        //
        ECNamedCurveParameterSpec secp256k1 = ECNamedCurveTable.getParameterSpec("secp256k1");
        //通过随机值初始化密钥对生成器
        ecdsaKeypairGenerator.initialize(secp256k1, new SecureRandom());
        //返回创建的密钥对
        return ecdsaKeypairGenerator.generateKeyPair();
    }

    public static float getBalance(String address){
        //获得某个地址的全部余额
        BlockChain blockChainObject = BlockChain.getBlockChainObject();
        assert blockChainObject != null;
        List<UtxoInfo> utxoInfoList = blockChainObject.findAllUtxos(address);
        float balance = 0.0f;
        //首先要遍历所有的UTXO
        for(UtxoInfo utxoInfo:utxoInfoList){
            //将UTXO中的余额取出来相加
            balance += utxoInfo.output.value;
        }
        return balance;
    }


    //利用公钥生成地址
    public String getAddress() {
        try {
            //获得ripemdHashedkey
            byte[] ripeMD160Hash = ripeMD160Hash(this.publicKey);
            //添加版本信息
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write((byte) 0);
            baos.write(ripeMD160Hash);
            byte[] versionPayload = baos.toByteArray();
            //计算校验码
            byte[] checksum = checksum(versionPayload);
            //得到version+payload+checksum的组合
            baos.write(checksum);
            //执行base58编码
            byte[] byteAddress = baos.toByteArray();
            return Base58Check.bytesToBase58(byteAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("get new wallet address failed!");
    }

    /**
     * 计算公钥的 RIPEMD160 Hash值
     *
     * @param pubKey 公钥
     * @return ipeMD160Hash(sha256 ( pubkey))
     */
    public static byte[] ripeMD160Hash(byte[] pubKey) {
        //1. 先对公钥做 sha256 处理
        byte[] shaHashedKey = DigestUtils.sha256(pubKey);
        RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
        ripemd160.update(shaHashedKey, 0, shaHashedKey.length);
        byte[] output = new byte[ripemd160.getDigestSize()];
        ripemd160.doFinal(output, 0);
        return output;
    }


    /**
     * 生成公钥的校验码
     *
     * @param payload
     * @return
     */
    public static byte[] checksum(byte[] payload) {
        return Arrays.copyOfRange(doubleHash(payload), 0, 4);
    }

    /**
     * 双重Hash
     *
     * @param data
     * @return
     */
    public static byte[] doubleHash(byte[] data) {
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }

}
