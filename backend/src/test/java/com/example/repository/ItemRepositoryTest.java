package com.example.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.Item;

/**
 * Unit tests for {@link ItemRepository}.
 * <p>
 * Tests: findAll, save (auto-increment ID), update (existing/non-existing),
 * deleteAll (existing/non-existing/empty list), concurrent access.
 */
class ItemRepositoryTest {

    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository = new ItemRepository();
    }

    // --- findAll tests ---

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        List<Item> items = itemRepository.findAll();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void findAll_afterSave_returnsAllItems() {
        itemRepository.save(new Item("Item 1", "Desc 1", LocalDate.now()));
        itemRepository.save(new Item("Item 2", "Desc 2", LocalDate.now()));

        List<Item> items = itemRepository.findAll();
        assertEquals(2, items.size());
    }

    // --- save tests ---

    @Test
    void save_assignsAutoIncrementId() {
        Item item1 = itemRepository.save(new Item("A", "Desc A", LocalDate.now()));
        Item item2 = itemRepository.save(new Item("B", "Desc B", LocalDate.now()));
        Item item3 = itemRepository.save(new Item("C", "Desc C", LocalDate.now()));

        assertEquals(1L, item1.getId());
        assertEquals(2L, item2.getId());
        assertEquals(3L, item3.getId());
    }

    @Test
    void save_nullItem_returnsNull() {
        Item result = itemRepository.save(null);
        assertNull(result);
        assertTrue(itemRepository.findAll().isEmpty());
    }

    @Test
    void save_preservesItemData() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        Item saved = itemRepository.save(new Item("Widget", "Widget's description", date));

        assertNotNull(saved);
        assertEquals("Widget", saved.getName());
        assertEquals("Widget's description", saved.getDescription());
        assertEquals(date, saved.getDate());
        assertTrue(saved.getId() > 0);
    }

    // --- update tests ---

    @Test
    void update_existingItem_replacesInMap() {
        Item original = itemRepository.save(new Item("Original", "Original desc", LocalDate.now()));
        long id = original.getId();

        Item updated = new Item("Updated", "Updated desc", LocalDate.of(2025, 6, 1));
        updated.setId(id);
        itemRepository.update(updated);

        List<Item> all = itemRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("Updated", all.get(0).getName());
        assertEquals("Updated desc", all.get(0).getDescription());
    }

    @Test
    void update_nonExistingItem_isNoOp() {
        itemRepository.save(new Item("Existing", "Desc", LocalDate.now()));

        Item ghost = new Item("Ghost", "Ghost desc", LocalDate.now());
        ghost.setId(999L);
        itemRepository.update(ghost);

        List<Item> all = itemRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("Existing", all.get(0).getName());
    }

    @Test
    void update_nullItem_isNoOp() {
        itemRepository.save(new Item("Existing", "Desc", LocalDate.now()));
        itemRepository.update(null);

        assertEquals(1, itemRepository.findAll().size());
    }

    // --- deleteAll tests ---

    @Test
    void deleteAll_existingIds_removesItems() {
        Item item1 = itemRepository.save(new Item("A", "Desc A", LocalDate.now()));
        Item item2 = itemRepository.save(new Item("B", "Desc B", LocalDate.now()));
        Item item3 = itemRepository.save(new Item("C", "Desc C", LocalDate.now()));

        itemRepository.deleteAll(Arrays.asList(item1.getId(), item3.getId()));

        List<Item> remaining = itemRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals("B", remaining.get(0).getName());
    }

    @Test
    void deleteAll_nonExistingIds_isNoOp() {
        itemRepository.save(new Item("A", "Desc A", LocalDate.now()));

        itemRepository.deleteAll(Arrays.asList(999L, 1000L));

        assertEquals(1, itemRepository.findAll().size());
    }

    @Test
    void deleteAll_emptyList_isNoOp() {
        itemRepository.save(new Item("A", "Desc A", LocalDate.now()));

        itemRepository.deleteAll(Collections.emptyList());

        assertEquals(1, itemRepository.findAll().size());
    }

    @Test
    void deleteAll_nullList_isNoOp() {
        itemRepository.save(new Item("A", "Desc A", LocalDate.now()));

        itemRepository.deleteAll(null);

        assertEquals(1, itemRepository.findAll().size());
    }

    // --- concurrent access tests ---

    @Test
    void concurrentSave_allItemsSavedWithUniqueIds() throws InterruptedException {
        int threadCount = 10;
        int itemsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < itemsPerThread; i++) {
                        itemRepository.save(new Item("Item", "Desc", LocalDate.now()));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        List<Item> all = itemRepository.findAll();
        assertEquals(threadCount * itemsPerThread, all.size());

        // Verify all IDs are unique
        long uniqueIds = all.stream().map(Item::getId).distinct().count();
        assertEquals(threadCount * itemsPerThread, uniqueIds);
    }
}
