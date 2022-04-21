package com.patrikmaryska.isprojekt.socsetreni.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("")
public class HtmlController {
    @GetMapping("")
    public String showApp(){
        return "login.html";
    }
}
