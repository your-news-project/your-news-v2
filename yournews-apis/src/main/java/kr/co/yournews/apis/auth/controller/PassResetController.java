package kr.co.yournews.apis.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PassResetController {

    @GetMapping("/pass-reset")
    public String passResetPage() {
        return "pass-reset";
    }
}
