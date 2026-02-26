package com.example.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.exception.ItemServiceException;
import com.example.model.Item;
import com.example.repository.ItemRepository;

/**
 * Service handling item CRUD operations.
 * <p>
 * Rewritten from: com.example.server.service.ItemServiceImpl
 * <p>
 * Business Logic:
 * <ul>
 *   <li>BL-006: findAll — delegate to repository</li>
 *   <li>BL-007: create — delegate to repository.save()</li>
 *   <li>BL-008: update — set ID and delegate to repository.update()</li>
 *   <li>BL-009: delete — delegate to repository.deleteAll()</li>
 * </ul>
 * All operations wrapped in try-catch, throwing ItemServiceException on failure
 * (matching source pattern).
 */
@Service
public class ItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Return all items.
     * <p>
     * Source: ItemServiceImpl.findAll(String sessionId)
     * BL-006: Returns all items from the repository.
     *
     * @return list of all items
     * @throws ItemServiceException if any error occurs
     */
    public List<Item> findAll() {
        try {
            return itemRepository.findAll();
        } catch (Exception ex) {
            LOGGER.error("Error finding all items", ex);
            throw new ItemServiceException(ex);
        }
    }

    /**
     * Create a new item.
     * <p>
     * Source: ItemServiceImpl.create(String sessionId, Item item)
     * BL-007: Delegate to repository.save() which assigns auto-increment ID.
     *
     * @param item the item to create
     * @return the created item with assigned ID
     * @throws ItemServiceException if any error occurs
     */
    public Item create(Item item) {
        try {
            return itemRepository.save(item);
        } catch (Exception ex) {
            LOGGER.error("Error creating item", ex);
            throw new ItemServiceException(ex);
        }
    }

    /**
     * Update an existing item.
     * <p>
     * Source: ItemServiceImpl.update(String sessionId, Item item)
     * BL-008: Set the ID on the item and delegate to repository.update().
     * Non-existent IDs are silently ignored (matching source behavior).
     *
     * @param id   the item ID
     * @param item the item data to update
     * @throws ItemServiceException if any error occurs
     */
    public void update(long id, Item item) {
        try {
            item.setId(id);
            itemRepository.update(item);
        } catch (Exception ex) {
            LOGGER.error("Error updating item with id {}", id, ex);
            throw new ItemServiceException(ex);
        }
    }

    /**
     * Delete items by their IDs.
     * <p>
     * Source: ItemServiceImpl.delete(String sessionId, ArrayList&lt;Item&gt; items)
     * BL-009: Delegate to repository.deleteAll(). Non-existent IDs silently ignored.
     *
     * @param ids the list of item IDs to delete
     * @throws ItemServiceException if any error occurs
     */
    public void delete(List<Long> ids) {
        try {
            itemRepository.deleteAll(ids);
        } catch (Exception ex) {
            LOGGER.error("Error deleting items", ex);
            throw new ItemServiceException(ex);
        }
    }
}
