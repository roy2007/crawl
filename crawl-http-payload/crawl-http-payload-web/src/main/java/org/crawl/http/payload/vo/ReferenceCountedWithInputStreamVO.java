package org.crawl.http.payload.vo;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 *
 * @author Roy
 *
 * @date 2022年1月8日-下午6:15:44
 */
public class ReferenceCountedWithInputStreamVO implements Closeable{

    private volatile int                                                              refrenceCount;
    private InputStream   inputStream;
    // private final int maxRefrenceCount = 5;
    private final int                                                                 increment     = 1;
    private final int                                                                 decrement     = 1;
    private static final AtomicIntegerFieldUpdater<ReferenceCountedWithInputStreamVO> REF_COUNT_UPDATER = AtomicIntegerFieldUpdater
                    .newUpdater (ReferenceCountedWithInputStreamVO.class, "refrenceCount");

    public ReferenceCountedWithInputStreamVO (InputStream in) {
        this.inputStream = in;
        REF_COUNT_UPDATER.set (this, increment);
    }

    public int getRefrenceCount () {
        return refrenceCount;
    }

    /**
     * An unsafe operation intended for use by a subclass that sets the reference count of the buffer directly
     */
    protected final void setRefrenceCount (int refrenceCount) {
        REF_COUNT_UPDATER.set (this, refrenceCount);
    }

    /**
     * @return the inputStream
     */
    public InputStream retain () {
        int oldRefrenceCount = REF_COUNT_UPDATER.getAndAdd (this, increment);
        if (oldRefrenceCount <= 0 || oldRefrenceCount + increment < oldRefrenceCount) {
            // Ensure we don't resurrect (which means the refCnt was 0) and also that we encountered an overflow.
            REF_COUNT_UPDATER.getAndAdd (this, -increment);
            throw new RuntimeException ("refrenceCount: " + refrenceCount + ", "
                            + ("increment: " + increment));
        }
        return inputStream;
    }

    @Override
    public void close () throws IOException {

        int oldRefrenceCount = REF_COUNT_UPDATER.getAndAdd (this, -decrement);
        System.out.println ("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ioStream 对象" + this.getClass ().getSimpleName ()
                        + "关闭方法!" + oldRefrenceCount);
        if (inputStream != null) {
            if (oldRefrenceCount == 0) {
                inputStream.close ();
                inputStream = null;
                this.refrenceCount = 0;
                return;
            } else if (oldRefrenceCount < decrement || oldRefrenceCount - decrement > oldRefrenceCount) {
                // Ensure we don't over-release, and avoid underflow.
                REF_COUNT_UPDATER.getAndAdd (this, decrement);
                throw new RuntimeException ("refrenceCount: " + refrenceCount + ", "
                                + ("increment: " + increment));
            }
        }
    }
}
