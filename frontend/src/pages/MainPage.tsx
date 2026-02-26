/**
 * MainPage — Main application view with item CRUD operations.
 *
 * Rewrite of MainView.java + MainPresenter.java into a single React page
 * that composes ItemTable, ItemDialog, and integrates with AuthContext and
 * the item API.
 *
 * Layout (mirrors MainView.ui.xml structure):
 *   Header:  Welcome {username} text + Logout hyperlink
 *   Center:  ItemTable component
 *   Footer:  Refresh button, Delete button, New button + error label
 *
 * Behavioral mapping:
 *   MainPresenter.loginHandler        → useEffect on mount: fetch items (T048)
 *   MainPresenter.refreshClickHandler → Refresh button (T048)
 *   MainPresenter.newClickHandler     → New button → ItemDialog create mode (T050)
 *   MainPresenter.onSelectedItem      → Row click → ItemDialog edit mode (T051)
 *   MainPresenter.deleteClickHandler  → Delete button → batch delete (T052)
 *   MainPresenter.logoutClickHandler  → Logout link (T053)
 *   MainPresenter.handleThrowable     → Error display in footer (T054)
 *   MainPresenter.serviceCallback     → Refresh after create/update/delete
 *
 * Business Logic references:
 *   BL-002: Logout flow
 *   BL-006: Find all items
 *   BL-007: Create item
 *   BL-008: Update item
 *   BL-009: Delete items
 *   BL-012: Error handling (401 handled by client.ts, 500 shown in error label)
 *
 * Source references:
 *   - src/main/java/com/example/client/view/MainView.java
 *   - src/main/java/com/example/client/presenter/MainPresenter.java
 */

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import * as itemApi from '../api/itemApi';
import type { Item } from '../types';
import ItemTable, { type ItemTableHandle } from '../components/ItemTable';
import ItemDialog from '../components/ItemDialog';

const MainPage: React.FC = () => {
  const { user, logout } = useAuth();

  // Item list state
  const [items, setItems] = useState<Item[]>([]);
  // Error display state (mirrors MainView.errorLabel)
  const [error, setError] = useState<string>('');
  // Dialog state
  const [dialogOpen, setDialogOpen] = useState(false);
  /** The item being edited, or null for create mode. */
  const [editItem, setEditItem] = useState<Item | null>(null);

  // Ref to ItemTable for querying checked items (mirrors MainView.getSelectedItems())
  const tableRef = useRef<ItemTableHandle>(null);

  /**
   * Fetch all items from the backend.
   * Mirrors: MainPresenter.findCallback → display.setItems(list)
   * Also mirrors: MainPresenter.loginHandler → itemService.findAll(sid, findCallback)
   * BL-006: findAll
   */
  const fetchItems = useCallback(async () => {
    try {
      setError(''); // Clear error on successful operation start
      const list = await itemApi.findAll();
      setItems(list);
      // Clear selection state on refresh (T048 requirement)
      tableRef.current?.clearSelection();
    } catch (err) {
      // BL-012: 401 is handled by client.ts interceptor (redirect to /login)
      // Other errors (500) are shown in the footer error label
      const message =
        err instanceof Error ? err.message : 'An unexpected error occurred';
      setError(message);
    }
  }, []);

  /**
   * Auto-fetch items on mount (after login/session recovery).
   * Mirrors: MainPresenter.loginHandler → itemService.findAll(sid, findCallback)
   * T048: auto-fetch on mount
   */
  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  /**
   * Refresh button handler.
   * Mirrors: MainPresenter.refreshClickHandler → itemService.findAll(sid, findCallback)
   * T048: Refresh re-fetches all items, clears selection.
   */
  const handleRefresh = () => {
    fetchItems();
  };

  /**
   * New button handler — opens ItemDialog in create mode.
   * Mirrors: MainPresenter.newClickHandler → itemPresenter.showView(new Item())
   * T050: New → create mode (empty fields, default date today)
   */
  const handleNew = () => {
    setEditItem(null);
    setDialogOpen(true);
  };

  /**
   * Row click handler — opens ItemDialog in edit mode.
   * Mirrors: MainPresenter.onSelectedItem → itemPresenter.showView(item)
   * T051: Row click → edit mode (pre-populated from selected item)
   */
  const handleRowClick = (item: Item) => {
    setEditItem(item);
    setDialogOpen(true);
  };

  /**
   * Dialog save handler.
   * Mirrors:
   *   ItemPresenter.saveHandler:
   *     - if item.id > 0 → eventBus.fireEvent(ItemUpdateEvent) → T051/BL-008
   *     - else           → eventBus.fireEvent(ItemCreateEvent) → T050/BL-007
   *   MainPresenter.createItemHandler → itemService.create(sid, item, serviceCallback)
   *   MainPresenter.updateItemHandler → itemService.update(sid, item, serviceCallback)
   *   serviceCallback.onSuccess       → refreshClickHandler.onClick(null)
   */
  const handleSave = async (data: Omit<Item, 'id'> & { id?: number }) => {
    try {
      setError('');
      if (data.id && data.id > 0) {
        // Edit mode — PUT /api/items/{id} (BL-008)
        await itemApi.update(data as Item);
      } else {
        // Create mode — POST /api/items (BL-007)
        await itemApi.create({
          name: data.name,
          description: data.description,
          date: data.date,
        });
      }
      // Close dialog (mirrors ItemPresenter: display.removePopUp())
      setDialogOpen(false);
      setEditItem(null);
      // Refresh item list (mirrors serviceCallback.onSuccess → refreshClickHandler)
      await fetchItems();
    } catch (err) {
      // BL-012: error handling
      const message =
        err instanceof Error ? err.message : 'An unexpected error occurred';
      setError(message);
    }
  };

  /**
   * Dialog cancel/close handler.
   * Mirrors: ItemPresenter.cancelHandler → display.removePopUp()
   */
  const handleDialogClose = () => {
    setDialogOpen(false);
    setEditItem(null);
  };

  /**
   * Delete button handler.
   * Mirrors: MainPresenter.deleteClickHandler:
   *   - display.getSelectedItems() → if list.size() > 0 → itemService.delete(sid, list, serviceCallback)
   * T052: Collect checked item IDs; if none, skip; otherwise DELETE /api/items
   * BL-009: batch delete
   */
  const handleDelete = async () => {
    const checkedIds = tableRef.current?.getCheckedIds() ?? [];
    // If none selected, skip — no API call (mirrors: if(list.size() > 0))
    if (checkedIds.length === 0) {
      return;
    }
    try {
      setError('');
      await itemApi.deleteItems(checkedIds);
      // Refresh item list (mirrors serviceCallback.onSuccess)
      await fetchItems();
    } catch (err) {
      // BL-012: error handling
      const message =
        err instanceof Error ? err.message : 'An unexpected error occurred';
      setError(message);
    }
  };

  /**
   * Logout link handler.
   * Mirrors: MainPresenter.logoutClickHandler:
   *   - userService.logout(sid, callback) → no-op on success/failure
   *   - cookies.removeCookies("sid")
   * T053/BL-002: POST /api/auth/logout → clear auth state
   * Note: user is NOT auto-redirected here (source behavior: SessionTimedOutEvent handles redirect).
   * The AuthContext.logout() clears state, then ProtectedRoute will redirect to /login.
   */
  const handleLogout = async () => {
    await logout();
  };

  return (
    <div style={{ padding: '1rem', maxWidth: '800px', margin: '0 auto' }}>
      {/* Header — Welcome text + Logout link */}
      {/* Mirrors: MainView.nameLabel (welcome text) + MainView.logoutLink (Hyperlink) */}
      <header
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '1rem',
          borderBottom: '1px solid #ccc',
          paddingBottom: '0.5rem',
        }}
      >
        <span data-testid="welcome-text">
          Welcome {user?.username ?? ''}
        </span>
        <a
          href="#"
          data-testid="logout-link"
          onClick={(e) => {
            e.preventDefault();
            handleLogout();
          }}
        >
          Logout
        </a>
      </header>

      {/* Center — ItemTable component (T046) */}
      <div style={{ marginBottom: '1rem', border: '1px solid #ccc', overflowX: 'auto' }}>
        <ItemTable
          ref={tableRef}
          items={items}
          onRowClick={handleRowClick}
        />
      </div>

      {/* Footer — Buttons + error label */}
      {/* Mirrors: MainView.refreshButton, deleteButton, newButton, errorLabel */}
      <footer style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
        <button onClick={handleRefresh} data-testid="refresh-button">
          Refresh
        </button>
        <button onClick={handleDelete} data-testid="delete-button">
          Delete
        </button>
        <button onClick={handleNew} data-testid="new-button">
          New
        </button>
        {/* Error label — T054: item API errors (500) shown here */}
        {error && (
          <span
            role="alert"
            data-testid="error-label"
            style={{ color: 'red', marginLeft: '1rem' }}
          >
            {error}
          </span>
        )}
      </footer>

      {/* ItemDialog — create/edit modal (T049) */}
      <ItemDialog
        open={dialogOpen}
        item={editItem}
        onSave={handleSave}
        onClose={handleDialogClose}
      />
    </div>
  );
};

export default MainPage;
