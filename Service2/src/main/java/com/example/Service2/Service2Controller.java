package com.example.Service2;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service2")
public class Service2Controller {

    @GetMapping({"", "/**"})
    public String getService2() {
        return "R Get de Servicio 2";
    }

    @PostMapping({"", "/**"})
    public String postService2() {
        return "C Post de Servicio 2";
    }

    @PatchMapping({"", "/**"})
    public String patchService2() {
        return "U Patch de Servicio 2";
    }

    @DeleteMapping({"", "/**"})
    public String deleteService2() {
        return "D Delete de Servicio 2";
    }
}
