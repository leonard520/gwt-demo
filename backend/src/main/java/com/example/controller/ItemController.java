package com.example.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.DeleteRequest;
import com.example.model.Item;
import com.example.service.ItemService;

/**
 * REST controller for item CRUD operations.
 * <p>
 * Replaces the GWT-RPC ItemService interface and RpcController front-controller
 * session validation pattern with Spring Security-protected REST endpoints.
 * <p>
 * Source: com.example.server.service.ItemServiceImpl (findAll, create, update, delete)
 * Source: com.example.server.controller.RpcController (handleRequest, validateSession)
 * <p>
 * Business Logic:
 * <ul>
 *   <li>BL-006: Find All Items — GET /api/items</li>
 *   <li>BL-007: Create Item — POST /api/items</li>
 *   <li>BL-008: Update Item — PUT /api/items/{id}</li>
 *   <li>BL-009: Delete Items (Batch) — DELETE /api/items</li>
 * </ul>
 * <p>
 * All endpoints require authentication (enforced by Spring Security filter chain,
 * replacing the RpcController.validateSession() pattern from the source).
 * <p>
 * Requirements: REQ-006, REQ-007, REQ-008, REQ-009, REQ-010, REQ-022
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * GET /api/items — Returns all items.
     * <p>
     * API Contract: returns List&lt;Item&gt; (200).
     * <p>
     * Source: ItemServiceImpl.findAll(String sessionId) → ArrayList&lt;Item&gt;
     * BL-006: Returns all items from the repository; order not guaranteed.
     *
     * @return list of all items
     */
    @GetMapping
    public ResponseEntity<List<Item>> findAll() {
        List<Item> items = itemService.findAll();
        return ResponseEntity.ok(items);
    }

    /**
     * POST /api/items — Creates a new item with server-assigned ID.
     * <p>
     * API Contract: accepts Item (name, description, date), returns created Item with ID (201).
     * <p>
     * Source: ItemServiceImpl.create(String sessionId, Item item) → void
     * Note: Source returned void; target returns created item with server-assigned ID.
     * BL-007: Delegate to repository.save() which assigns auto-increment ID.
     *
     * @param item the item to create (ID is ignored; server assigns a new one)
     * @return the created item with server-assigned ID
     */
    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        LOGGER.debug("Creating item: {}", item.getName());
        Item created = itemService.create(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/items/{id} — Updates an existing item by ID.
     * <p>
     * API Contract: accepts Item body + path ID, returns updated Item (200).
     * Non-existent IDs are silently ignored (no-op, matching source behavior).
     * <p>
     * Source: ItemServiceImpl.update(String sessionId, Item item) → void
     * Note: Source passed ID in body; target uses URL path parameter.
     * BL-008: Set ID from path, delegate to repository.update().
     *
     * @param id   the item ID from the URL path
     * @param item the item data to update
     * @return the updated item
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable long id, @RequestBody Item item) {
        LOGGER.debug("Updating item with id: {}", id);
        item.setId(id);
        itemService.update(id, item);
        return ResponseEntity.ok(item);
    }

    /**
     * DELETE /api/items — Deletes items by their IDs (batch delete).
     * <p>
     * API Contract: accepts DeleteRequest with ids array, returns 204 No Content.
     * Non-existent IDs are silently ignored (matching source behavior).
     * <p>
     * Source: ItemServiceImpl.delete(String sessionId, ArrayList&lt;Item&gt; items) → void
     * Note: Source passed full item objects; target passes only IDs.
     * BL-009: Delegate to repository.deleteAll().
     *
     * @param deleteRequest the batch delete request containing item IDs
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody DeleteRequest deleteRequest) {
        LOGGER.debug("Deleting items with ids: {}", deleteRequest.getIds());
        itemService.delete(deleteRequest.getIds());
        return ResponseEntity.noContent().build();
    }
}
