package com.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Item}.
 * <p>
 * Rewritten from: com.example.client.model.ItemTest
 * Uses JUnit 5 (Jupiter) instead of JUnit 4.
 */
class ItemTest {

    private Item item;

    private static final String NAME = "name";
    private static final String DESC = "description";
    private static final LocalDate DATE = LocalDate.of(2025, 1, 15);

    @BeforeEach
    void setUp() {
        item = new Item();
    }

    // --- Constructor tests ---

    @Test
    void testDefaultConstructor() {
        assertNotNull(item);
        assertEquals(0, item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertNotNull(item.getDate(), "Date should default to current date");
    }

    @Test
    void testThreeArgConstructor() {
        item = new Item(NAME, DESC, DATE);
        assertEquals(NAME, item.getName());
        assertEquals(DESC, item.getDescription());
        assertEquals(DATE, item.getDate());
        assertEquals(0, item.getId(), "ID should be 0 before server assignment");
    }

    // --- Getter/Setter tests ---

    @Test
    void testGetId() {
        assertEquals(0, item.getId());
    }

    @Test
    void testSetId() {
        item.setId(1L);
        assertEquals(1L, item.getId());
    }

    @Test
    void testGetName() {
        assertNull(item.getName());
    }

    @Test
    void testSetName() {
        item.setName(NAME);
        assertEquals(NAME, item.getName());
    }

    @Test
    void testGetDescription() {
        assertNull(item.getDescription());
    }

    @Test
    void testSetDescription() {
        item.setDescription(DESC);
        assertEquals(DESC, item.getDescription());
    }

    @Test
    void testGetDate() {
        assertNotNull(item.getDate(), "Date should never be null on default construction");
    }

    @Test
    void testSetDate() {
        item.setDate(DATE);
        assertEquals(DATE, item.getDate());
    }

    // --- equals() tests ---

    @Test
    void testEqualsIdentity() {
        assertEquals(item, item, "Same reference should be equal");
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(null, item, "Item should not equal null");
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals("not an item", item, "Item should not equal a different class");
    }

    @Test
    void testEqualsSameValues() {
        Item a = new Item(NAME, DESC, DATE);
        a.setId(1L);
        Item b = new Item(NAME, DESC, DATE);
        b.setId(1L);
        assertEquals(a, b, "Items with same field values should be equal");
    }

    @Test
    void testEqualsDifferentId() {
        Item a = new Item(NAME, DESC, DATE);
        a.setId(1L);
        Item b = new Item(NAME, DESC, DATE);
        b.setId(2L);
        assertNotEquals(a, b, "Items with different IDs should not be equal");
    }

    @Test
    void testEqualsDifferentName() {
        Item a = new Item(NAME, DESC, DATE);
        Item b = new Item("other", DESC, DATE);
        assertNotEquals(a, b, "Items with different names should not be equal");
    }

    @Test
    void testEqualsDifferentDescription() {
        Item a = new Item(NAME, DESC, DATE);
        Item b = new Item(NAME, "other", DATE);
        assertNotEquals(a, b, "Items with different descriptions should not be equal");
    }

    @Test
    void testEqualsDifferentDate() {
        Item a = new Item(NAME, DESC, DATE);
        Item b = new Item(NAME, DESC, LocalDate.of(2024, 6, 1));
        assertNotEquals(a, b, "Items with different dates should not be equal");
    }

    @Test
    void testEqualsNullFields() {
        Item a = new Item();
        Item b = new Item();
        assertEquals(a, b, "Two default items should be equal");
    }

    @Test
    void testEqualsOneNullName() {
        Item a = new Item();
        a.setDate(DATE);
        Item b = new Item();
        b.setName(NAME);
        b.setDate(DATE);
        assertNotEquals(a, b, "Item with null name should not equal item with non-null name");
    }

    @Test
    void testEqualsOneNullDescription() {
        Item a = new Item();
        a.setDate(DATE);
        Item b = new Item();
        b.setDescription(DESC);
        b.setDate(DATE);
        assertNotEquals(a, b, "Item with null description should not equal item with non-null description");
    }

    @Test
    void testEqualsOneNullDate() {
        Item a = new Item();
        a.setDate(null);
        Item b = new Item();
        b.setDate(DATE);
        assertNotEquals(a, b, "Item with null date should not equal item with non-null date");
    }

    // --- hashCode() tests ---

    @Test
    void testHashCodeConsistency() {
        item = new Item(NAME, DESC, DATE);
        item.setId(1L);
        int hash1 = item.hashCode();
        int hash2 = item.hashCode();
        assertEquals(hash1, hash2, "hashCode should be consistent across calls");
    }

    @Test
    void testHashCodeEqualObjects() {
        Item a = new Item(NAME, DESC, DATE);
        a.setId(1L);
        Item b = new Item(NAME, DESC, DATE);
        b.setId(1L);
        assertEquals(a.hashCode(), b.hashCode(), "Equal objects must have same hashCode");
    }

    @Test
    void testHashCodeDefaultItems() {
        Item a = new Item();
        a.setDate(DATE);
        Item b = new Item();
        b.setDate(DATE);
        assertEquals(a.hashCode(), b.hashCode(), "Default items with same date should have same hashCode");
    }

    // --- toString() tests ---

    @Test
    void testToString() {
        item = new Item(NAME, DESC, DATE);
        item.setId(5L);
        String result = item.toString();
        assertNotNull(result);
        assertEquals("Item [date=" + DATE + ", description=" + DESC + ", id=5, name=" + NAME + "]", result);
    }

    @Test
    void testToStringDefaultItem() {
        // Verify toString does not throw on default-constructed item
        String result = item.toString();
        assertNotNull(result);
    }
}
