package io.eddie.unitybe.dashboard.controller;

import io.eddie.unitybe.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// @RestController가 아니라 @Controller — JSON이 아니라 HTML(뷰)을 반환한다.
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /admin → templates/dashboard.html 렌더링. 집계 결과를 model에 담아 넘긴다.
    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("summary", dashboardService.getSummary());
        return "dashboard";
    }
}
