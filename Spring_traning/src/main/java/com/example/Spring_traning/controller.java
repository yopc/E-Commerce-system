package com.example.Spring_traning;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class controller {

    @RequestMapping("/hello")
    public  String hello()
    {
        return "hello";
    }
}
