/**
 * ItemTable — Data table with selectable rows and checkboxes.
 *
 * Rewrite of MainView.java's FlexTable-based item list into a React component.
 *
 * Column layout (mirrors GWT column widths):
 *   - Name:        128px
 *   - Description: 192px
 *   - Date:        256px
 *   - Checkbox:     25px
 *
 * Behavioral mapping:
 *   MainView.initTable()           → <thead> with column widths
 *   MainView.setItems()            → rows rendered from items prop
 *   MainView.onTableClicked()      → onClick handler: cell.getCellIndex() != 3 → row click
 *   MainView.selectRow()           → selectedRow state + CSS class
 *   MainView.styleRow()            → conditional className on <tr>
 *   MainView.getSelectedItems()    → checkedIds tracked via internal state, exposed via prop callback
 *
 * Key behavior:
 *   - Clicking a checkbox does NOT trigger row selection (mirrors cell.getCellIndex() != 3 guard)
 *   - Clicking any other cell in a row fires onRowClick with that item
 *   - Selected row gets highlighted with a CSS class
 *
 * Source references:
 *   - src/main/java/com/example/client/view/MainView.java#setItems,getSelectedItems,onTableClicked
 *   - src/main/java/com/example/client/presenter/MainPresenter.java#onSelectedItem
 */

import { useState, useCallback, useImperativeHandle, forwardRef } from 'react';
import type { Item } from '../types';

/** Public handle so parent components can query checked items (mirrors MainView.getSelectedItems). */
export interface ItemTableHandle {
  getCheckedIds: () => number[];
  clearSelection: () => void;
}

interface ItemTableProps {
  items: Item[];
  /** Called when user clicks a non-checkbox cell in a row. */
  onRowClick: (item: Item) => void;
}

const ItemTable = forwardRef<ItemTableHandle, ItemTableProps>(
  ({ items, onRowClick }, ref) => {
    /** Set of checked item IDs (checkbox state). */
    const [checkedIds, setCheckedIds] = useState<Set<number>>(new Set());
    /** Currently highlighted row index (-1 = none). Mirrors MainView.selectedRow. */
    const [selectedRow, setSelectedRow] = useState<number>(-1);

    // Expose imperative handle so MainPage can call getCheckedIds / clearSelection
    useImperativeHandle(ref, () => ({
      getCheckedIds: () => Array.from(checkedIds),
      clearSelection: () => {
        setCheckedIds(new Set());
        setSelectedRow(-1);
      },
    }));

    /**
     * Handle checkbox toggle.
     * Mirrors: checkbox click does NOT trigger row selection (cell.getCellIndex() != 3 guard).
     */
    const handleCheckboxChange = useCallback(
      (itemId: number) => {
        setCheckedIds((prev) => {
          const next = new Set(prev);
          if (next.has(itemId)) {
            next.delete(itemId);
          } else {
            next.add(itemId);
          }
          return next;
        });
      },
      [],
    );

    /**
     * Handle row click on non-checkbox cells.
     * Mirrors: MainView.onTableClicked → selectRow(row) → listener.onSelectedItem(item)
     */
    const handleRowClick = useCallback(
      (item: Item, rowIndex: number) => {
        setSelectedRow(rowIndex);
        onRowClick(item);
      },
      [onRowClick],
    );

    return (
      <table
        style={{ width: '100%', borderCollapse: 'collapse' }}
        role="table"
      >
        {/* Column widths matching GWT: 128px / 192px / 256px / 25px */}
        <colgroup>
          <col style={{ width: '128px' }} />
          <col style={{ width: '192px' }} />
          <col style={{ width: '256px' }} />
          <col style={{ width: '25px' }} />
        </colgroup>

        <thead>
          <tr>
            <th style={{ textAlign: 'left' }}>Name</th>
            <th style={{ textAlign: 'left' }}>Description</th>
            <th style={{ textAlign: 'left' }}>Date</th>
            <th>{/* checkbox column — empty header, mirrors header.setText(0,3," ") */}</th>
          </tr>
        </thead>

        <tbody>
          {items.map((item, index) => (
            <tr
              key={item.id}
              data-testid={`item-row-${item.id}`}
              className={selectedRow === index ? 'selected-row' : ''}
              style={{
                cursor: 'pointer',
                backgroundColor: selectedRow === index ? '#cfdaec' : undefined,
              }}
            >
              {/* Name cell — click triggers row selection */}
              <td
                onClick={() => handleRowClick(item, index)}
                data-testid={`cell-name-${item.id}`}
              >
                {item.name}
              </td>
              {/* Description cell — click triggers row selection */}
              <td
                onClick={() => handleRowClick(item, index)}
                data-testid={`cell-desc-${item.id}`}
              >
                {item.description}
              </td>
              {/* Date cell — click triggers row selection */}
              <td
                onClick={() => handleRowClick(item, index)}
                data-testid={`cell-date-${item.id}`}
              >
                {item.date}
              </td>
              {/* Checkbox cell — click does NOT trigger row selection */}
              <td
                onClick={(e) => e.stopPropagation()}
                style={{ textAlign: 'center' }}
              >
                <input
                  type="checkbox"
                  aria-label={`Select ${item.name}`}
                  checked={checkedIds.has(item.id)}
                  onChange={() => handleCheckboxChange(item.id)}
                  data-testid={`checkbox-${item.id}`}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  },
);

ItemTable.displayName = 'ItemTable';

export default ItemTable;
