package com.handederelii.bom_project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @GetMapping("/public/hello")
    public String publicHello() {
        return "Merhaba! Bu endpoint herkese açık olmalı.";
    }

    @GetMapping("/secure/hello")
    public String secureHello() {
        return "Merhaba! Bu endpoint sadece giriş yapmış (authenticated) kullanıcıya açık.";
    }
}