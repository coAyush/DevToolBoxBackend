package com.devtoolbox.DevToolBoxRevamp.comm;

public class ResponseHelper {

    public static ApiResponse success(String message){
        return ApiResponse.builder()
                .code(0)
                .message(message)
                .status("Success")
                .build();
    }

    public static ApiResponse error(String message){
        return ApiResponse.builder()
                .code(8)
                .message(message)
                .status("Error")
                .build();
    }

    public static ApiResponse commonSuccess(String message,Metadata data){
        return ApiResponse.builder()
                .code(0)
                .message(message)
                .status("Success")
                .data(data)
                .build();
    }

    public static ApiResponse commonError(String message,Metadata data){
        return ApiResponse.builder()
                .code(8)
                .message(message)
                .status("Error")
                .data(data)
                .build();
    }
}
