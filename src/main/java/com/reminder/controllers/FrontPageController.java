package com.reminder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontPageController {

    @GetMapping("/reminders")
    public String remindersPage() {
        return "reminders";
    }
}
