
package org.crawl.http.result;

import java.io.Serializable;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:25:37
 */
public class Result<T> implements Serializable {

    /**
     * DataResult.java -long
     */
    private static final long serialVersionUID = -3130624019865503141L;
    private String code = "200";
    private String message = "成功";
    private boolean success = true;
    private T data;

    public Result(T data) {
        this.data = data;
    }

    public Result() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public static <D> Result<D> with(D data) {
        Result<D> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static <D> Result<D> ok() {
        return new Result<D>();
    }

//        public static <D> Result<D> error(ErrorCode errorCode, String... params) {
//            return error(errorCode.getCode(), errorCode.getMessage((Object[])params));
//        }
//
//        public static <D> Result<D> sysError(String... params) {
//            if (params == null || params.length == 0) {
//                params = new String[] {""};
//            }
//            return error(ErrorCode.SYSTEM_ERR, params);
//        }

        /**
         * 通过错误码生成失败result
         * 
         *    puberrorCode
         */
//        public void fail(ErrorCode errorCode, Object... paramArr) {
//            this.setSuccess(Boolean.FALSE);
//            this.setCode(errorCode.getCode());
//            this.setMessage(errorCode.getMessage());
//            if (paramArr != null && paramArr.length > 0) {
//                this.setMessage(String.format(errorCode.getMessage(), Arrays.asList(paramArr).toArray()));
//            }
//        }

}