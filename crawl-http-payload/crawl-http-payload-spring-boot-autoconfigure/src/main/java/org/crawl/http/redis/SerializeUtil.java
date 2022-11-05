
package org.crawl.http.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:37:30
 */
public class SerializeUtil {

    /**
     * 序列化
     * @param object
     * @author corleone
     * @date 2018年11月27日
     */
    public static byte[] serizlize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 反序列化
     * @param bytes
     * @author corleone
     * @date 2018年11月27日
     */
    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }
}
