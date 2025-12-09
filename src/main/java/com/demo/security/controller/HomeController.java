package com.demo.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index", "/index.html"})
    public String home() {
        // Redirect root requests to the products page so the index template is not shown
        return "redirect:/products";
    }

    @GetMapping({"/login", "/login.html"})
    public String loginPage() {
        return "login";
    }

    @GetMapping({"/register", "/register.html"})
    public String registerPage() {
        return "register";
    }

    @GetMapping({"/products", "/products.html"})
    public String productsPage() {
        return "products";
    }

    @GetMapping({"/admin", "/admin.html"})
    public String adminPage() {
        return "admin";
    }
}