/**
 * Tests for ItemDialog component.
 *
 * Mirrors test patterns from the original GWT ItemPresenterTest:
 *   - testItemPresenter      → dialog renders fields and buttons
 *   - testShowView           → showPopUp with item data (edit mode)
 *   - testSaveHandlerCreate  → save in create mode fires ItemCreateEvent
 *   - testSaveHandlerUpdate  → save in edit mode fires ItemUpdateEvent
 *   - testClickHandler       → cancel closes dialog
 *
 * Source references:
 *   - src/test/java/com/example/client/presenter/ItemPresenterTest.java
 *   - src/main/java/com/example/client/view/ItemView.java
 *   - src/main/java/com/example/client/presenter/ItemPresenter.java
 */

import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import ItemDialog from '../components/ItemDialog';
import type { Item } from '../types';

// --- Mock data ---

const mockItem: Item = {
  id: 1,
  name: 'Test Item',
  description: 'Test Description',
  date: '2024-06-15',
};

// --- Helpers ---

const mockOnSave = jest.fn();
const mockOnClose = jest.fn();

function renderDialog(props: Partial<React.ComponentProps<typeof ItemDialog>> = {}) {
  return render(
    <ItemDialog
      open={true}
      item={null}
      onSave={mockOnSave}
      onClose={mockOnClose}
      {...props}
    />,
  );
}

// --- Test Suite ---

beforeEach(() => {
  jest.clearAllMocks();
});

describe('ItemDialog', () => {
  /**
   * Mirrors: testItemPresenter — verifies construction and bindings.
   * Verifies dialog renders with all required elements.
   */
  test('renders dialog with title, name/description/date fields, Save and Cancel buttons', () => {
    renderDialog();

    // Title — mirrors DialogBox.setText("Create/Edit an Item")
    expect(screen.getByRole('dialog')).toHaveAttribute('aria-label', 'Create/Edit an Item');
    expect(screen.getByText('Create/Edit an Item')).toBeInTheDocument();

    // Input fields
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Description')).toBeInTheDocument();
    expect(screen.getByLabelText('Date')).toBeInTheDocument();

    // Buttons
    expect(screen.getByText('Save')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  /**
   * Create mode: fields should be empty (name/description), date defaults to today.
   * Mirrors: new Item() — default constructor creates empty item.
   */
  test('create mode renders empty name/description fields', () => {
    renderDialog({ item: null });

    expect(screen.getByLabelText('Name')).toHaveValue('');
    expect(screen.getByLabelText('Description')).toHaveValue('');
    // Date defaults to today
    const dateInput = screen.getByLabelText('Date') as HTMLInputElement;
    const today = new Date().toISOString().split('T')[0];
    expect(dateInput.value).toBe(today);
  });

  /**
   * Mirrors: testShowView — showPopUp(item) populates fields.
   * Edit mode: fields pre-populated from the provided item.
   */
  test('edit mode pre-populates fields from item', () => {
    renderDialog({ item: mockItem });

    expect(screen.getByLabelText('Name')).toHaveValue('Test Item');
    expect(screen.getByLabelText('Description')).toHaveValue('Test Description');
    expect(screen.getByLabelText('Date')).toHaveValue('2024-06-15');
  });

  /**
   * Mirrors: testSaveHandlerCreate — save with no id → fires ItemCreateEvent.
   * Create mode save: onSave called WITHOUT id.
   */
  test('create mode Save calls onSave without id', () => {
    renderDialog({ item: null });

    // Fill fields
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'New Item' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'New Desc' } });
    fireEvent.change(screen.getByLabelText('Date'), { target: { value: '2024-07-01' } });

    // Click Save
    fireEvent.click(screen.getByText('Save'));

    expect(mockOnSave).toHaveBeenCalledWith({
      name: 'New Item',
      description: 'New Desc',
      date: '2024-07-01',
    });
  });

  /**
   * Mirrors: testSaveHandlerUpdate — save with id > 0 → fires ItemUpdateEvent.
   * Edit mode save: onSave called WITH id.
   */
  test('edit mode Save calls onSave with id', () => {
    renderDialog({ item: mockItem });

    // Modify name
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Updated Name' } });

    // Click Save
    fireEvent.click(screen.getByText('Save'));

    expect(mockOnSave).toHaveBeenCalledWith({
      id: 1,
      name: 'Updated Name',
      description: 'Test Description',
      date: '2024-06-15',
    });
  });

  /**
   * Mirrors: testClickHandler — cancel handler calls display.removePopUp().
   * Cancel button calls onClose.
   */
  test('Cancel button calls onClose', () => {
    renderDialog();

    fireEvent.click(screen.getByText('Cancel'));

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  /**
   * Glass overlay click also closes dialog.
   * Mirrors: glass-enabled DialogBox behavior.
   */
  test('clicking glass overlay calls onClose', () => {
    renderDialog();

    fireEvent.click(screen.getByTestId('dialog-overlay'));

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  /**
   * When open=false, dialog should not render.
   */
  test('dialog does not render when open is false', () => {
    renderDialog({ open: false });

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  /**
   * Glass overlay renders with dimmed background.
   * Mirrors: DialogBox.setGlassEnabled(true) — dimmed background.
   */
  test('glass overlay has dimmed background', () => {
    renderDialog();

    const overlay = screen.getByTestId('dialog-overlay');
    expect(overlay).toBeInTheDocument();
    expect(overlay.style.backgroundColor).toBe('rgba(0, 0, 0, 0.5)');
  });

  /**
   * Verify dialog is centered with fixed positioning.
   * Mirrors: DialogBox.center().
   */
  test('dialog is centered with fixed positioning', () => {
    renderDialog();

    const dialog = screen.getByRole('dialog');
    expect(dialog.style.position).toBe('fixed');
    expect(dialog.style.top).toBe('50%');
    expect(dialog.style.left).toBe('50%');
  });
});
