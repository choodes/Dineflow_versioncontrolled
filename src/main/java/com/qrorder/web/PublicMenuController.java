package com.qrorder.web;

import com.qrorder.model.MenuItem;
import com.qrorder.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/menu")
public class PublicMenuController {

    private final MenuService menuService;

    public PublicMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItem> menu() {
        return menuService.getLiveMenu();
    }

    /** Suggests starters/desserts to pair with a chosen main dish. */
    @GetMapping("/recommendations")
    public List<MenuItem> recommendations(@RequestParam Long mainItemId) {
        return menuService.recommendationsFor(mainItemId);
    }
}
