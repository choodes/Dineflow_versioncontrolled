package com.qrorder.config;

import com.qrorder.model.MenuCategory;
import com.qrorder.model.MenuItem;
import com.qrorder.model.RestaurantTable;
import com.qrorder.repo.MenuItemRepository;
import com.qrorder.repo.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;

    public DataSeeder(TableRepository tableRepository, MenuItemRepository menuItemRepository) {
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(String... args) {
        if (tableRepository.count() == 0) {
            for (int i = 1; i <= 6; i++) {
                tableRepository.save(new RestaurantTable("Table " + i, "table-token-" + i));
            }
        }

        if (menuItemRepository.count() == 0) {
            MenuItem paneerTikka = save("Paneer Tikka", "Char-grilled cottage cheese in smoked yogurt marinade.",
                    "249", MenuCategory.STARTER);
            MenuItem springRolls = save("Veg Spring Rolls", "Crisp rolls with a spiced vegetable filling.",
                    "199", MenuCategory.STARTER);
            MenuItem gulabJamun = save("Gulab Jamun", "Warm milk dumplings in cardamom rose syrup.",
                    "99", MenuCategory.DESSERT);
            MenuItem iceCream = save("Vanilla Ice Cream", "Two scoops, classic vanilla bean.",
                    "89", MenuCategory.DESSERT);

            MenuItem butterChicken = save("Butter Chicken", "Slow-simmered tomato-butter curry with tender chicken.",
                    "349", MenuCategory.MAIN);
            butterChicken.setPairsWithIds(paneerTikka.getId() + "," + gulabJamun.getId());
            menuItemRepository.save(butterChicken);

            MenuItem biryani = save("Hyderabadi Biryani", "Basmati rice layered with spiced vegetables, dum-cooked.",
                    "329", MenuCategory.MAIN);
            biryani.setPairsWithIds(springRolls.getId() + "," + iceCream.getId());
            menuItemRepository.save(biryani);

            save("Masala Dosa", "Crisp rice crepe filled with spiced potato.", "179", MenuCategory.MAIN);
            save("Sweet Lassi", "Chilled, lightly sweetened yogurt drink.", "79", MenuCategory.BEVERAGE);
            save("Masala Chai", "Spiced milk tea, brewed fresh.", "49", MenuCategory.BEVERAGE);
        }
    }

    private MenuItem save(String name, String desc, String price, MenuCategory category) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(desc);
        item.setPrice(new BigDecimal(price));
        item.setCategory(category);
        item.setAvailable(true);
        return menuItemRepository.save(item);
    }
}
