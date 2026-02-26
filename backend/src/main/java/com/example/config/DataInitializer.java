package com.example.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.model.Item;
import com.example.model.User;
import com.example.repository.ItemRepository;
import com.example.repository.UserRepository;

/**
 * Seeds initial data on application startup.
 * <p>
 * Rewritten from:
 * <ul>
 *   <li>com.example.server.dao.UserDaoImpl — seeded 2 users (davis/davis, homer/homer)</li>
 *   <li>com.example.server.dao.ItemDaoImpl.createItems() — seeded 9 items</li>
 * </ul>
 * Uses BCryptPasswordEncoder (via PasswordEncoder) instead of the custom BCrypt class.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ItemRepository itemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedItems();
        LOGGER.info("Data initialization complete: 2 users and 9 items seeded.");
    }

    /**
     * Seed users matching source UserDaoImpl constructor:
     * <pre>
     *   map.put("davis", new User("davis", BCrypt.hashpw("davis", BCrypt.gensalt())));
     *   map.put("homer", new User("homer", BCrypt.hashpw("homer", BCrypt.gensalt())));
     * </pre>
     */
    private void seedUsers() {
        userRepository.save(new User("davis", passwordEncoder.encode("davis")));
        userRepository.save(new User("homer", passwordEncoder.encode("homer")));
        LOGGER.debug("Seeded users: davis, homer");
    }

    /**
     * Seed items matching source ItemDaoImpl.createItems():
     * <pre>
     *   create(new Item("Item 1", "Description of Item 1", randomDate()));
     *   create(new Item("Item 2", "Description of Item 2", randomDate()));
     *   create(new Item("Foo", "Foo's description", randomDate()));
     *   create(new Item("Bar", "Bar's description", randomDate()));
     *   create(new Item("Baz", "Baz's description", randomDate()));
     *   create(new Item("Widget", "Widget's description", randomDate()));
     *   create(new Item("FooBar", "Description of FooBar ", randomDate()));
     *   create(new Item("BarFoo", "Bar Foo", randomDate()));
     *   create(new Item("FooBaz", "Foo Baz", randomDate()));
     * </pre>
     * Note: Source used randomDate(); rewrite uses LocalDate.now() for deterministic seed data.
     */
    private void seedItems() {
        LocalDate today = LocalDate.now();
        itemRepository.save(new Item("Item 1", "Description of Item 1", today));
        itemRepository.save(new Item("Item 2", "Description of Item 2", today));
        itemRepository.save(new Item("Foo", "Foo's description", today));
        itemRepository.save(new Item("Bar", "Bar's description", today));
        itemRepository.save(new Item("Baz", "Baz's description", today));
        itemRepository.save(new Item("Widget", "Widget's description", today));
        itemRepository.save(new Item("FooBar", "Description of FooBar ", today));
        itemRepository.save(new Item("BarFoo", "Bar Foo", today));
        itemRepository.save(new Item("FooBaz", "Foo Baz", today));
        LOGGER.debug("Seeded 9 items");
    }
}
