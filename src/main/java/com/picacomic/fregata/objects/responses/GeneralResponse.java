package com.picacomic.fregata.objects.responses;


/**
 * 通用响应信封（code、message、data）。
 * Generic response envelope (code, message, data).
 */
public class GeneralResponse<DataClass> {
    public int code;

    public DataClass data;
    public String message;

    public GeneralResponse(DataClass data) {
        this.data = data;
    }

    public String toString() {
        return "GeneralResponse{code=" + this.code + ", message='" + this.message + "', data=" + this.data + '}';
    }
}
