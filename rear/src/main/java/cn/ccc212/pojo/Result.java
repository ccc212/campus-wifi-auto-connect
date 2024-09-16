package cn.ccc212.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> implements Serializable {
    private Integer code;

    private String msg;

    private T data;

    public static <T> Result<T> success(){
        return new Result<>(1,"success",null);
    }

    public static <T> Result<T> success(T data){
        return new Result<>(1,"success",data);
    }

    public static <T> Result<T> error(String msg){
        return new Result<>(0,msg,null);
    }
}
