package com.example.Service1;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service1")
public class Service1Controller {

    @GetMapping({"", "/**"})
    public String getService1() {
        return "R Get de Servicio 1";
    }

    @PostMapping({"", "/**"})
    public String postService1() {
        return "C Post de Servicio 1";
    }

    @PatchMapping({"", "/**"})
    public String patchService1() {
        return "U Patch de Servicio 1";
    }

    @DeleteMapping({"", "/**"})
    public String deleteService1() {
        return "D Delete de Servicio 1";
    }
}
