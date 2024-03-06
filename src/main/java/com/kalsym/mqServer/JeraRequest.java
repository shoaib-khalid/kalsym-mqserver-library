package com.kalsym.mqServer;

/**
 *
 * @author Ali Khan
 */
public abstract class JeraRequest implements Runnable {

    public String RawIncomingXml;

    @Override
    public abstract void run();

    public abstract void decode();
}
