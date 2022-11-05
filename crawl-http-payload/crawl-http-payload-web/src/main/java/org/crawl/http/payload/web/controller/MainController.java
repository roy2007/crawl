package org.crawl.http.payload.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Roy
 * @date 2020/11/05
 */
@Controller
public class MainController {

    @RequestMapping("/")
    public String testThymeleaf(Model model) {
        model.addAttribute("msg", "Hello, this is thymeleaf");
        return "main";
    }

}
