package com.javaisnotdead.dstbug.ui;

import com.javaisnotdead.dstbug.repository.BuggyRepository;
import com.javaisnotdead.dstbug.repository.FixedRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final BuggyRepository buggyRepository;
    private final FixedRepository fixedRepository;

    public MainController(BuggyRepository buggyRepository, FixedRepository fixedRepository) {
        this.buggyRepository = buggyRepository;
        this.fixedRepository = fixedRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("buggyRows", buggyRepository.findAll());
        model.addAttribute("fixedRows", fixedRepository.findAll());
        return "index";
    }
}
