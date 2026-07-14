package com.vedantu.ext.cmds.managers;

public class SDCardHardwareManager extends AbstractManager {

    private long size;

    public SDCardHardwareManager(long size) {

        this.size = size;
    }

    public boolean isValidSDCardInserted() {

        if (checkIfSDCardExist() && hasFreespace(size) && checkIfAlreadyWritten()) {
            return true;
        }
        return false;
    }

    public boolean format() {

        return false;
    }

    private boolean checkIfSDCardExist() {

        return false;
    }

    private boolean hasFreespace(long size) {

        return false;
    }

    private boolean checkIfAlreadyWritten() {

        return false;
    }

}
