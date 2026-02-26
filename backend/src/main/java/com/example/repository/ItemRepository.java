package com.example.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.example.model.Item;

/**
 * In-memory item repository using ConcurrentHashMap + AtomicLong sequence.
 * <p>
 * Rewritten from: com.example.server.dao.ItemDaoImpl
 * Preserves exact CRUD semantics:
 * <ul>
 *   <li>save() assigns auto-increment ID via AtomicLong.incrementAndGet() [BL-007]</li>
 *   <li>update() is a no-op if ID not found (silent ignore) [BL-008]</li>
 *   <li>deleteAll() silently ignores non-existent IDs [BL-009]</li>
 *   <li>findAll() returns a copy of all values [BL-006]</li>
 * </ul>
 */
@Component
public class ItemRepository {

    private final Map<Long, Item> map = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();

    /**
     * Return all items.
     * <p>
     * Source: ItemDaoImpl.findAll() — returned new ArrayList containing all values.
     *
     * @return a list of all items (order not guaranteed)
     */
    public List<Item> findAll() {
        return new ArrayList<>(map.values());
    }

    /**
     * Save a new item with an auto-assigned ID.
     * <p>
     * Source: ItemDaoImpl.create(Item) — null items are silently ignored;
     * ID assigned via sequence.incrementAndGet().
     *
     * @param item the item to save
     * @return the saved item with assigned ID, or null if input was null
     */
    public Item save(Item item) {
        if (item == null) {
            return null;
        }
        item.setId(sequence.incrementAndGet());
        map.put(item.getId(), item);
        return item;
    }

    /**
     * Update an existing item by ID.
     * <p>
     * Source: ItemDaoImpl.update(Item) — no-op if item is null or ID not in map.
     *
     * @param item the item to update
     */
    public void update(Item item) {
        if (item == null || !map.containsKey(item.getId())) {
            return;
        }
        map.put(item.getId(), item);
    }

    /**
     * Delete items by their IDs.
     * <p>
     * Source: ItemDaoImpl.delete(ArrayList&lt;Item&gt;) — null/empty list is a no-op;
     * non-existent IDs silently ignored by map.remove().
     *
     * @param ids the list of item IDs to delete
     */
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            map.remove(id);
        }
    }
}
