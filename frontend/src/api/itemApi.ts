/**
 * Item CRUD API functions.
 *
 * Maps the GWT-RPC ItemService/ItemServiceAsync interface to REST calls:
 *   - ItemService.findAll(sessionId)           → GET    /api/items
 *   - ItemService.create(sessionId, item)      → POST   /api/items
 *   - ItemService.update(sessionId, item)      → PUT    /api/items/{id}
 *   - ItemService.delete(sessionId, items)     → DELETE /api/items
 *
 * Source references:
 *   - src/main/java/com/example/client/service/ItemService.java
 *   - src/main/java/com/example/client/service/ItemServiceAsync.java
 */

import { apiRequest } from './client';
import type { Item, DeleteRequest } from '../types';

/**
 * Fetches all items from the backend.
 * Source: ItemService.findAll (BL-006)
 */
export function findAll(): Promise<Item[]> {
  return apiRequest<Item[]>('/api/items', {
    method: 'GET',
  });
}

/**
 * Creates a new item. The server assigns the ID.
 * Source: ItemService.create (BL-007)
 */
export function create(item: Omit<Item, 'id'>): Promise<Item> {
  return apiRequest<Item>('/api/items', {
    method: 'POST',
    body: JSON.stringify(item),
  });
}

/**
 * Updates an existing item by ID.
 * Source: ItemService.update (BL-008)
 */
export function update(item: Item): Promise<Item> {
  return apiRequest<Item>(`/api/items/${item.id}`, {
    method: 'PUT',
    body: JSON.stringify({
      name: item.name,
      description: item.description,
      date: item.date,
    }),
  });
}

/**
 * Batch-deletes items by their IDs.
 * Source: ItemService.delete (BL-009)
 */
export function deleteItems(ids: number[]): Promise<void> {
  const body: DeleteRequest = { ids };
  return apiRequest<void>('/api/items', {
    method: 'DELETE',
    body: JSON.stringify(body),
  });
}
