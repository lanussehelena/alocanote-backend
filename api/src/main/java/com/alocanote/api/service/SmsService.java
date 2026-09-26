package com.alocanote.api.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendVerificationSms(String phoneNumber, String code) {
        // Simulação do envio de SMS (Mock)
        System.out.println("----------------------------------------");
        System.out.println("[SMS MOCK] Enviando código " + code + " para o número: " + phoneNumber);
        System.out.println("----------------------------------------");
    }
}