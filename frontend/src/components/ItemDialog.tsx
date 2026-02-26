/**
 * ItemDialog — Modal dialog for creating / editing an Item.
 *
 * Rewrite of ItemView.java + ItemPresenter.java into a single React component.
 *
 * Behavioral mapping:
 *   ItemView.showPopUp(item)       → open state + pre-populated fields (edit) or empty (create)
 *   ItemView.removePopUp()         → onClose callback
 *   DialogBox.setGlassEnabled(true)→ glass overlay (dimmed background)
 *   DialogBox.setAnimationEnabled  → CSS animation on open
 *   DialogBox.center()             → centered positioning
 *   DialogBox.setText("Create/Edit an Item") → dialog title
 *   ItemView fields                → name, description, date inputs
 *   ItemPresenter.saveHandler      → onSave: if item.id > 0 → update, else → create
 *   ItemPresenter.cancelHandler    → onClose
 *
 * Source references:
 *   - src/main/java/com/example/client/view/ItemView.java#showPopUp,setItem
 *   - src/main/java/com/example/client/presenter/ItemPresenter.java#saveHandler,cancelHandler
 */

import React, { useState, useEffect } from 'react';
import type { Item } from '../types';

interface ItemDialogProps {
  /** Whether the dialog is visible. */
  open: boolean;
  /** Item to edit (edit mode) or null/undefined (create mode). */
  item?: Item | null;
  /** Called with the item data when Save is clicked. */
  onSave: (item: Omit<Item, 'id'> & { id?: number }) => void;
  /** Called when Cancel is clicked or glass overlay is dismissed. */
  onClose: () => void;
}

/**
 * Format today's date as YYYY-MM-DD for the default date in create mode.
 */
function todayISO(): string {
  return new Date().toISOString().split('T')[0];
}

const ItemDialog: React.FC<ItemDialogProps> = ({ open, item, onSave, onClose }) => {
  const isEditMode = !!(item && item.id && item.id > 0);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState(todayISO());

  /**
   * Populate fields when dialog opens.
   * Edit mode: pre-populated from selected item (mirrors ItemView.showPopUp → setText/setValue).
   * Create mode: empty fields, default date today (mirrors new Item() defaults).
   */
  useEffect(() => {
    if (open) {
      if (isEditMode && item) {
        setName(item.name);
        setDescription(item.description);
        setDate(item.date);
      } else {
        setName('');
        setDescription('');
        setDate(todayISO());
      }
    }
  }, [open, item, isEditMode]);

  if (!open) return null;

  /**
   * Save handler.
   * Mirrors ItemPresenter.saveHandler:
   *   - Gathers field values (display.getItem())
   *   - If id > 0 → fires ItemUpdateEvent; else → fires ItemCreateEvent
   *   - Calls display.removePopUp()
   */
  const handleSave = () => {
    const data: Omit<Item, 'id'> & { id?: number } = {
      name,
      description,
      date,
    };
    if (isEditMode && item) {
      data.id = item.id;
    }
    onSave(data);
  };

  return (
    <>
      {/* Glass overlay — mirrors DialogBox.setGlassEnabled(true) */}
      <div
        data-testid="dialog-overlay"
        onClick={onClose}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          zIndex: 1000,
        }}
      />

      {/* Centered dialog — mirrors DialogBox.center() + animation */}
      <div
        role="dialog"
        aria-label="Create/Edit an Item"
        style={{
          position: 'fixed',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          backgroundColor: '#fff',
          border: '1px solid #ccc',
          borderRadius: '8px',
          padding: '1.5rem',
          zIndex: 1001,
          minWidth: '360px',
          animation: 'fadeIn 0.2s ease-in',
        }}
      >
        {/* Title — mirrors DialogBox.setText("Create/Edit an Item") */}
        <h2 style={{ marginTop: 0 }}>Create/Edit an Item</h2>

        {/* Name field — mirrors ItemView.nameBox (TextBox) */}
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="item-name">Name</label>
          <input
            id="item-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            style={{
              display: 'block',
              width: '100%',
              marginTop: '0.25rem',
              padding: '0.5rem',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* Description field — mirrors ItemView.descriptionBox (TextBox) */}
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="item-description">Description</label>
          <input
            id="item-description"
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            style={{
              display: 'block',
              width: '100%',
              marginTop: '0.25rem',
              padding: '0.5rem',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* Date field — mirrors ItemView.datePicker (DatePicker) */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label htmlFor="item-date">Date</label>
          <input
            id="item-date"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            style={{
              display: 'block',
              width: '100%',
              marginTop: '0.25rem',
              padding: '0.5rem',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* Buttons — mirrors ItemView: saveButton + cancelButton */}
        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
          <button onClick={handleSave}>Save</button>
          <button onClick={onClose}>Cancel</button>
        </div>
      </div>
    </>
  );
};

export default ItemDialog;
