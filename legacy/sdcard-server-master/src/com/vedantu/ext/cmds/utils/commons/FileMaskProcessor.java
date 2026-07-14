package com.vedantu.ext.cmds.utils.commons;

import java.util.Arrays;

public class FileMaskProcessor {

    private final String passPhrase;
    private final int    passBytesSize;
    private final byte[] passPhraseBytes;

    public FileMaskProcessor(final String passPhrase, final int passBytesSize) {

        this.passPhrase = passPhrase;
        this.passBytesSize = passBytesSize;
        this.passPhraseBytes = passPhraseToByteArray();
    }

    private byte[] passPhraseToByteArray() {

        byte[] t = passPhrase.getBytes();
        byte[] p = Arrays.copyOfRange(t, 0, passBytesSize);
        return p;
    }

    public String process(String inString) {

        byte[] outBytes = new byte[inString.length()];
        process(inString.getBytes(), 0, outBytes.length, outBytes);
        return new String(outBytes);
    }

    public void process(byte[] bytes, int startPoint, int length, byte[] result) {

        byte[] t = Arrays.copyOfRange(bytes, 0, length);
        byte[] p = getPassPhrase(startPoint, length);
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (t[i] ^ p[i]);
        }
    }

    private byte[] getPassPhrase(int startPoint, int length) {

        int remainingFromLastBoundary = startPoint % passBytesSize;
        int tempLength = remainingFromLastBoundary + length;
        if ((tempLength % passBytesSize) != 0) {
            tempLength = ((tempLength / passBytesSize) + 1) * passBytesSize;
        }

        byte[] tempPassPhrase = new byte[tempLength];
        for (int i = 0, j = 0; i < tempLength; i++, j = (j + 1) % passBytesSize) {
            tempPassPhrase[i] = passPhraseBytes[j];
        }

        byte[] resultPassPhrase = Arrays.copyOfRange(tempPassPhrase, remainingFromLastBoundary,
                remainingFromLastBoundary + length);
        return resultPassPhrase;
    }

}
