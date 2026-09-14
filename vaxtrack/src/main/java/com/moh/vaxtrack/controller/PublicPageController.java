package com.moh.vaxtrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicPageController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        return "public/home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activePage", "about");
        return "public/about";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("activePage", "services");
        return "public/services";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("activePage", "faq");
        return "public/faq";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activePage", "contact");
        return "public/contact";
    }
}
