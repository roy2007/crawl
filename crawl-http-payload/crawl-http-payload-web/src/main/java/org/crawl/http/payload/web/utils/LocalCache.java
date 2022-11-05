package org.crawl.http.payload.web.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.crawl.http.payload.vo.ReferenceCountedWithInputStreamVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Roy
 *
 */
@Slf4j
public class LocalCache {
    @SuppressWarnings("rawtypes")
    private static Map<String, CacheData> CACHE_DATA = new ConcurrentHashMap<>();
    private static List<? super Closeable> REMOVE_DATA = new ArrayList<> ();

    private static Timer                   timer       = new Timer ();
    static {
        timer.scheduleAtFixedRate (new ReleaseIoStream (), 0, 10000);
    }

    public static <T> T getData(String key, Load<T> load, int expire) {
        T data = getData(key);
        if (data == null && load != null) {
            data = load.load();
            if (data != null) {
                setData(key, data, expire);
            }
        }
        return data;
    }

    public static <T> T getData (String key) {
        @SuppressWarnings ("unchecked")
        CacheData<T> data = CACHE_DATA.get (key);
        if (data == null) {
            return null;
        }
        boolean isAlive = (data.getExpire () <= 0 || data.getSifeTime () >= System.currentTimeMillis ());
        if (isAlive) {
            return data.getData ();
        } else {
            remove (key);
            return null;
        }
    }

    public static <T> void setData(String key, T data, int expire) {
        CACHE_DATA.put(key, new CacheData<T>(data, expire));
    }

    public static <T> void remove (String key) {
        @SuppressWarnings ("unchecked")
        CacheData<T> data = CACHE_DATA.remove (key);
        if (data != null) {
            T bizData = data.getData ();
            if (bizData instanceof Closeable) {
                System.out.println ("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~过期数据，将删除" + bizData.toString ());
                Closeable closeableObj = (Closeable) bizData;
                REMOVE_DATA.add (closeableObj);
                // closeIoStream (closeableObj);
            }
        }
    }

    public static <T> void clearAll () {
        for (@SuppressWarnings ("rawtypes")
        Entry<String, CacheData> entry : CACHE_DATA.entrySet ()) {
            @SuppressWarnings ("unchecked")
            CacheData<T> data = entry.getValue ();
            if (data != null) {
                T bizData = data.getData ();
                if (bizData instanceof Closeable) {
                    Closeable closeableObj = (Closeable) bizData;
                    REMOVE_DATA.add (closeableObj);
                    closeIoStream (closeableObj);
                }
            }
        }
        CACHE_DATA.clear();
    }

    private static void closeIoStream (Closeable closableData) {
        try {
            System.out.println ("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~调用对象自关闭方法");
            closableData.close ();
        } catch (IOException e) {
            log.warn ("IO stream close, ignore error! ", e);
        }
    }

    public interface Load<T> {
        T load();
    }

    private static class CacheData<T> {
        CacheData(T t, int expire) {
            this.data = t;
            this.expire = expire <= 0 ? 0 : expire * 1000;
            this.sifeTime = System.currentTimeMillis() + this.expire;
        }

        private T data;
        private long sifeTime;
        /**
         * 小于0 永不过期
         */
        private long expire;


        /**
         * @return the data
         */
        public T getData() {
            return data;
        }

        /**
         * @return the sifeTime
         */
        public long getSifeTime() {
            return sifeTime;
        }

        /**
         * @return the expire
         */
        public long getExpire() {
            return expire;
        }
    }

    private static class ReleaseIoStream extends TimerTask{

        public void run () {
            System.out.println ("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Time's up!");
            Iterator<? super Closeable> it = REMOVE_DATA.iterator ();
            while (it.hasNext ()) {
                Closeable closableData = (Closeable) it.next ();
                closeIoStream (closableData);
                if (closableData instanceof ReferenceCountedWithInputStreamVO) {
                    ReferenceCountedWithInputStreamVO vo = (ReferenceCountedWithInputStreamVO) closableData;
                    if (vo.getRefrenceCount () == 0) {
                        vo = null;
                        System.out.println ("~~~~~~~~~~~~~~~~~真正删除引用~~~~~~~~~~~~~~~~~");
                        it.remove ();
                    }
                }
            }
            String currentThreadName = Thread.currentThread ().getName ();
            System.out.println ("~~~~~~~~~~~~~~~~" + currentThreadName + "|" + "will remove io stream size:"
                            + REMOVE_DATA.size ());
            System.out.println ("~~~~~~~~~~~~~~~~" + currentThreadName + "|" + "will cache size:" + CACHE_DATA.size ());
            /**  Terminate the timer thread */
            // timer.cancel ();
        }
    }

    /**
     * 每隔10秒获取一次
     * 
     * @param operatorInfo
     * @param parameterMap
     * @return
     */
    @SuppressWarnings("unused")
    private String getUserCreationGroupsCount(String key, Map<String, Object> parameterMap) {
        String mymakes = LocalCache.getData(key, new LocalCache.Load<String>() {
            @Override
            public String load() {
                String mymakes = "";// XXXXService.queryCountUsers(parameterMap);
                return mymakes;
            }
        }, 10);
        return mymakes;
    }
}
