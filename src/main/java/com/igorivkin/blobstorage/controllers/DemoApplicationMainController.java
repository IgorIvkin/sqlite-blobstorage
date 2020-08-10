package com.igorivkin.blobstorage.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DemoApplicationMainController {

    @RequestMapping(value = {"", "/"})
    public String renderMainPage() {
        return "main_page";
    }

}
