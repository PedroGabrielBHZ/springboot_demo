package com.comp.demo.rest;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @GetMapping(value = "/", produces = "text/html")
    public String sayHello() {
        return "<!DOCTYPE html><html><head><title>Willkommen.</title></head><body><h1>Welcome!</h1><p>This is a simple HTML welcome page.</p></body></html>";
    }
}
