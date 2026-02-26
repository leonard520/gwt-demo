/**
 * Tests for MainPage component.
 *
 * Mirrors test patterns from the original GWT MainPresenterTest:
 *   - testMainPresenter       → renders with header/table/buttons
 *   - loginHandler            → auto-fetch items on mount
 *   - newClickHandler         → create flow (New → dialog → save → refresh)
 *   - onSelectedItem          → edit flow (row click → dialog → save → refresh)
 *   - deleteClickHandler      → delete flow (checkbox select → Delete → refresh)
 *   - refreshClickHandler     → refresh re-fetches items
 *   - handleThrowable         → error display
 *   - logoutClickHandler      → logout flow
 *
 * Source references:
 *   - src/test/java/com/example/client/presenter/MainPresenterTest.java
 *   - src/main/java/com/example/client/presenter/MainPresenter.java
 */

import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import '@testing-library/jest-dom';
import MainPage from '../pages/MainPage';
import type { Item } from '../types';

// --- Mock data ---

const mockItems: Item[] = [
  { id: 1, name: 'Item One', description: 'Desc 1', date: '2024-01-15' },
  { id: 2, name: 'Item Two', description: 'Desc 2', date: '2024-02-20' },
  { id: 3, name: 'Item Three', description: 'Desc 3', date: '2024-03-25' },
];

// --- Mocks ---

const mockLogout = jest.fn().mockResolvedValue(undefined);

jest.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    user: { username: 'testuser' },
    token: null,
    isAuthenticated: true,
    loading: false,
    login: jest.fn(),
    logout: mockLogout,
    checkSession: jest.fn(),
    clearAuth: jest.fn(),
  }),
}));

const mockFindAll = jest.fn<Promise<Item[]>, []>();
const mockCreate = jest.fn<Promise<Item>, [Omit<Item, 'id'>]>();
const mockUpdate = jest.fn<Promise<Item>, [Item]>();
const mockDeleteItems = jest.fn<Promise<void>, [number[]]>();

jest.mock('../api/itemApi', () => ({
  findAll: (...args: []) => mockFindAll(...args),
  create: (...args: [Omit<Item, 'id'>]) => mockCreate(...args),
  update: (...args: [Item]) => mockUpdate(...args),
  deleteItems: (...args: [number[]]) => mockDeleteItems(...args),
}));

// --- Helpers ---

function renderMainPage() {
  return render(<MainPage />);
}

// --- Test Suite ---

beforeEach(() => {
  jest.clearAllMocks();
  mockFindAll.mockResolvedValue(mockItems);
  mockCreate.mockResolvedValue({ id: 4, name: 'New', description: 'New desc', date: '2024-04-01' });
  mockUpdate.mockResolvedValue({ ...mockItems[0], name: 'Updated' });
  mockDeleteItems.mockResolvedValue(undefined);
});

describe('MainPage', () => {
  /**
   * Mirrors: testMainPresenter — verifies construction binds all handlers.
   * The React equivalent verifies that the component renders with all
   * required UI elements: header, table, and footer buttons.
   */
  test('renders with header (welcome text + logout), table, and footer buttons', async () => {
    renderMainPage();

    // Wait for initial fetch
    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalled();
    });

    // Header: welcome text
    expect(screen.getByTestId('welcome-text')).toHaveTextContent('Welcome testuser');
    // Header: logout link
    expect(screen.getByTestId('logout-link')).toHaveTextContent('Logout');

    // Table: check items rendered
    expect(screen.getByText('Item One')).toBeInTheDocument();
    expect(screen.getByText('Item Two')).toBeInTheDocument();
    expect(screen.getByText('Item Three')).toBeInTheDocument();

    // Footer buttons
    expect(screen.getByTestId('refresh-button')).toHaveTextContent('Refresh');
    expect(screen.getByTestId('delete-button')).toHaveTextContent('Delete');
    expect(screen.getByTestId('new-button')).toHaveTextContent('New');
  });

  /**
   * Mirrors: MainPresenter.loginHandler → itemService.findAll(sid, findCallback)
   * T048: Auto-fetch items on mount.
   */
  test('fetches items on mount (auto-fetch after login/session recovery)', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalledTimes(1);
    });

    // Items are displayed in the table
    expect(screen.getByText('Item One')).toBeInTheDocument();
    expect(screen.getByText('Desc 1')).toBeInTheDocument();
    expect(screen.getByText('2024-01-15')).toBeInTheDocument();
  });

  /**
   * Mirrors: MainPresenter.refreshClickHandler → itemService.findAll(sid, findCallback)
   * T048: Refresh button re-fetches all items.
   */
  test('Refresh button re-fetches all items', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalledTimes(1);
    });

    // Click Refresh
    fireEvent.click(screen.getByTestId('refresh-button'));

    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalledTimes(2);
    });
  });

  /**
   * Mirrors: MainPresenter.newClickHandler → itemPresenter.showView(new Item())
   * T050: New button → opens ItemDialog in create mode → on Save: POST → refresh → close dialog.
   */
  test('New button opens dialog in create mode, Save creates item and refreshes', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalledTimes(1);
    });

    // Click New button
    fireEvent.click(screen.getByTestId('new-button'));

    // Dialog should be open
    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeInTheDocument();
    expect(dialog).toHaveAttribute('aria-label', 'Create/Edit an Item');

    // Fields should be empty (create mode)
    const nameInput = screen.getByLabelText('Name');
    const descInput = screen.getByLabelText('Description');
    expect(nameInput).toHaveValue('');
    expect(descInput).toHaveValue('');

    // Fill in fields
    fireEvent.change(nameInput, { target: { value: 'New Item' } });
    fireEvent.change(descInput, { target: { value: 'New Description' } });
    fireEvent.change(screen.getByLabelText('Date'), { target: { value: '2024-04-01' } });

    // Click Save
    fireEvent.click(within(dialog).getByText('Save'));

    await waitFor(() => {
      expect(mockCreate).toHaveBeenCalledWith({
        name: 'New Item',
        description: 'New Description',
        date: '2024-04-01',
      });
    });

    // Dialog should close
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    // Items should be re-fetched (refresh after create)
    await waitFor(() => {
      // initial fetch + refresh after create = 2
      expect(mockFindAll).toHaveBeenCalledTimes(2);
    });
  });

  /**
   * Mirrors: MainPresenter.onSelectedItem → itemPresenter.showView(item)
   * T051: Row click → opens ItemDialog in edit mode → on Save: PUT → refresh → close dialog.
   */
  test('Row click opens dialog in edit mode, Save updates item and refreshes', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    // Click on the Name cell of the first item (non-checkbox cell triggers row click)
    fireEvent.click(screen.getByTestId('cell-name-1'));

    // Dialog should be open with pre-populated fields
    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeInTheDocument();

    expect(screen.getByLabelText('Name')).toHaveValue('Item One');
    expect(screen.getByLabelText('Description')).toHaveValue('Desc 1');
    expect(screen.getByLabelText('Date')).toHaveValue('2024-01-15');

    // Modify name
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Updated Item One' } });

    // Click Save
    fireEvent.click(within(dialog).getByText('Save'));

    await waitFor(() => {
      expect(mockUpdate).toHaveBeenCalledWith({
        id: 1,
        name: 'Updated Item One',
        description: 'Desc 1',
        date: '2024-01-15',
      });
    });

    // Dialog should close
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    // Items re-fetched after update
    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalledTimes(2);
    });
  });

  /**
   * Mirrors: MainPresenter.deleteClickHandler
   * T052: Delete button → collect checked IDs → if none, skip; otherwise DELETE → refresh.
   */
  test('Delete button with checked items calls deleteItems and refreshes', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    // Check items 1 and 3
    fireEvent.click(screen.getByTestId('checkbox-1'));
    fireEvent.click(screen.getByTestId('checkbox-3'));

    // Click Delete
    fireEvent.click(screen.getByTestId('delete-button'));

    await waitFor(() => {
      expect(mockDeleteItems).toHaveBeenCalledWith(
        expect.arrayContaining([1, 3]),
      );
    });

    // Items re-fetched after delete
    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalledTimes(2);
    });
  });

  /**
   * Mirrors: MainPresenter.deleteClickHandler → if(list.size() > 0) guard
   * T052: Delete with no checked items should NOT call the API.
   */
  test('Delete button with no checked items does not call deleteItems', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    // Click Delete without checking any items
    fireEvent.click(screen.getByTestId('delete-button'));

    // Should NOT call the API
    expect(mockDeleteItems).not.toHaveBeenCalled();
    // Should NOT trigger a refresh (still just the initial fetch)
    expect(mockFindAll).toHaveBeenCalledTimes(1);
  });

  /**
   * T046: Checkbox click does NOT trigger row selection (onRowClick should not fire).
   */
  test('checkbox click does not trigger row selection or open dialog', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    // Click checkbox for first item
    fireEvent.click(screen.getByTestId('checkbox-1'));

    // Dialog should NOT open
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    // Checkbox should be checked
    expect(screen.getByTestId('checkbox-1')).toBeChecked();
  });

  /**
   * Mirrors: MainPresenter.logoutClickHandler
   * T053/BL-002: Logout link → POST /api/auth/logout → clear auth state.
   */
  test('Logout link calls logout', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalled();
    });

    // Click logout link
    fireEvent.click(screen.getByTestId('logout-link'));

    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalledTimes(1);
    });
  });

  /**
   * Mirrors: MainPresenter.handleThrowable → display.setErrorMsg(throwable.getMessage())
   * T054/BL-012: Item API errors (500) shown in footer error label.
   */
  test('fetch error displays error message in footer', async () => {
    mockFindAll.mockRejectedValueOnce(new Error('Internal Server Error'));

    renderMainPage();

    await waitFor(() => {
      expect(screen.getByTestId('error-label')).toHaveTextContent('Internal Server Error');
    });
  });

  /**
   * T054: Error clears on successful operation.
   */
  test('error clears on successful refresh after error', async () => {
    // First fetch fails
    mockFindAll.mockRejectedValueOnce(new Error('Server error'));

    renderMainPage();

    await waitFor(() => {
      expect(screen.getByTestId('error-label')).toHaveTextContent('Server error');
    });

    // Fix the mock for next call
    mockFindAll.mockResolvedValueOnce(mockItems);

    // Click Refresh
    fireEvent.click(screen.getByTestId('refresh-button'));

    await waitFor(() => {
      expect(screen.queryByTestId('error-label')).not.toBeInTheDocument();
    });
  });

  /**
   * T054: Create failure shows error.
   */
  test('create failure shows error in footer', async () => {
    mockCreate.mockRejectedValueOnce(new Error('Failed to create item'));

    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    // Open create dialog
    fireEvent.click(screen.getByTestId('new-button'));

    // Fill and save
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Test' } });
    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => {
      expect(screen.getByTestId('error-label')).toHaveTextContent('Failed to create item');
    });
  });

  /**
   * Dialog cancel closes without saving.
   * Mirrors: ItemPresenter.cancelHandler → display.removePopUp()
   */
  test('Cancel button in dialog closes without saving', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    // Open dialog
    fireEvent.click(screen.getByTestId('new-button'));
    expect(screen.getByRole('dialog')).toBeInTheDocument();

    // Click Cancel
    fireEvent.click(screen.getByText('Cancel'));

    // Dialog should close
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    // No create API call
    expect(mockCreate).not.toHaveBeenCalled();
  });

  /**
   * T046: Selected row gets visual highlighting.
   */
  test('clicking a row highlights it with selected-row class', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(screen.getByText('Item One')).toBeInTheDocument();
    });

    const row = screen.getByTestId('item-row-1');
    // Click the name cell to trigger row selection
    fireEvent.click(screen.getByTestId('cell-name-1'));

    // Row should have selected-row class
    expect(row).toHaveClass('selected-row');
  });

  /**
   * Table header renders all column headers.
   */
  test('table renders column headers: Name, Description, Date', async () => {
    renderMainPage();

    await waitFor(() => {
      expect(mockFindAll).toHaveBeenCalled();
    });

    const table = screen.getByRole('table');
    expect(within(table).getByText('Name')).toBeInTheDocument();
    expect(within(table).getByText('Description')).toBeInTheDocument();
    expect(within(table).getByText('Date')).toBeInTheDocument();
  });
});
