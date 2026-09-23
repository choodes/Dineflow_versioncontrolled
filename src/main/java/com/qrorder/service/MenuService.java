package com.qrorder.service;

import com.qrorder.model.MenuCategory;
import com.qrorder.model.MenuItem;
import com.qrorder.repo.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<MenuItem> getLiveMenu() {
        return menuItemRepository.findByAvailableTrue();
    }

    /**
     * "Recommendation of starters and dessert when select particular main
     * dish" - uses the explicit pairsWithIds on the main dish if set,
     * otherwise falls back to any two available starters/desserts.
     */
    public List<MenuItem> recommendationsFor(Long mainItemId) {
        MenuItem main = menuItemRepository.findById(mainItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        if (main.getPairsWithIds() != null && !main.getPairsWithIds().isBlank()) {
            List<Long> ids = Arrays.stream(main.getPairsWithIds().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .map(Long::parseLong).toList();
            List<MenuItem> result = new ArrayList<>();
            for (Long id : ids) {
                menuItemRepository.findById(id).ifPresent(result::add);
            }
            if (!result.isEmpty()) return result;
        }

        // fallback: one starter + one dessert, first available of each
        List<MenuItem> fallback = new ArrayList<>();
        menuItemRepository.findByCategoryAndAvailableTrue(MenuCategory.STARTER)
                .stream().findFirst().ifPresent(fallback::add);
        menuItemRepository.findByCategoryAndAvailableTrue(MenuCategory.DESSERT)
                .stream().findFirst().ifPresent(fallback::add);
        return fallback;
    }

    // ---- Manager CRUD ("Edit menu items") ----

    public List<MenuItem> getAllForManager() {
        return menuItemRepository.findAll();
    }

    public MenuItem create(MenuItem item) {
        return menuItemRepository.save(item);
    }

    public MenuItem update(Long id, MenuItem update) {
        MenuItem existing = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setPrice(update.getPrice());
        existing.setCategory(update.getCategory());
        existing.setAvailable(update.isAvailable());
        existing.setPairsWithIds(update.getPairsWithIds());
        return menuItemRepository.save(existing);
    }

    public void delete(Long id) {
        menuItemRepository.deleteById(id);
    }

    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
    }
}
