package cn.yangtaotech;

public class PowResult {
    byte[] hash;
    long nonce;

    public PowResult(byte[] hash, long nonce) {
        this.hash = hash;
        this.nonce = nonce;
    }
}
