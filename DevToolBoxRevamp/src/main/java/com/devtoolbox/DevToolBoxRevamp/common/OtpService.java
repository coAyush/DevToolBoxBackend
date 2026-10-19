package com.devtoolbox.DevToolBoxRevamp.common;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpService {

    private final  Integer OTP_LENGTH=6;

    public  String generateOtp(){
        int min= (int) Math.pow(10,OTP_LENGTH-1);
        int max= (int) Math.pow(10,OTP_LENGTH);
        String otp= String.valueOf(new Random().nextInt(min,max));
        return otp;
    }
}
