package com.example.dto;

import java.util.List;

/**
 * DTO for batch delete requests.
 * <p>
 * Maps to DELETE /api/items request body.
 * Source: ItemService.delete(String sessionId, ArrayList&lt;Item&gt; items)
 * Note: Source passed full item objects; target passes only IDs.
 */
public class DeleteRequest {

    private List<Long> ids;

    public DeleteRequest() {
    }

    public DeleteRequest(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
