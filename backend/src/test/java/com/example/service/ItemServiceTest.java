package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.exception.ItemServiceException;
import com.example.model.Item;
import com.example.repository.ItemRepository;

/**
 * Mockito-based unit tests for {@link ItemService}.
 * <p>
 * Tests: findAll, create, update, delete (batch), exception wrapping in ItemServiceException.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository);
    }

    // --- findAll tests ---

    @Test
    void findAll_delegatesToRepository() {
        List<Item> expected = Arrays.asList(
                new Item("Item 1", "Desc 1", LocalDate.now()),
                new Item("Item 2", "Desc 2", LocalDate.now())
        );
        when(itemRepository.findAll()).thenReturn(expected);

        List<Item> result = itemService.findAll();

        assertEquals(expected, result);
        verify(itemRepository).findAll();
    }

    @Test
    void findAll_whenRepositoryThrows_throwsItemServiceException() {
        when(itemRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThrows(ItemServiceException.class, () -> itemService.findAll());
        verify(itemRepository).findAll();
    }

    // --- create tests ---

    @Test
    void create_delegatesToRepositorySave() {
        Item input = new Item("New Item", "New Desc", LocalDate.now());
        Item saved = new Item("New Item", "New Desc", LocalDate.now());
        saved.setId(1L);
        when(itemRepository.save(input)).thenReturn(saved);

        Item result = itemService.create(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Item", result.getName());
        verify(itemRepository).save(input);
    }

    @Test
    void create_whenRepositoryThrows_throwsItemServiceException() {
        when(itemRepository.save(any())).thenThrow(new RuntimeException("Save error"));

        assertThrows(ItemServiceException.class,
                () -> itemService.create(new Item("X", "Y", LocalDate.now())));
        verify(itemRepository).save(any());
    }

    // --- update tests ---

    @Test
    void update_setsIdAndDelegatesToRepository() {
        Item item = new Item("Updated", "Updated desc", LocalDate.now());

        itemService.update(5L, item);

        assertEquals(5L, item.getId());
        verify(itemRepository).update(item);
    }

    @Test
    void update_whenRepositoryThrows_throwsItemServiceException() {
        doThrow(new RuntimeException("Update error")).when(itemRepository).update(any());

        assertThrows(ItemServiceException.class,
                () -> itemService.update(1L, new Item("X", "Y", LocalDate.now())));
        verify(itemRepository).update(any());
    }

    // --- delete tests ---

    @Test
    void delete_delegatesToRepositoryDeleteAll() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        itemService.delete(ids);

        verify(itemRepository).deleteAll(ids);
    }

    @Test
    void delete_whenRepositoryThrows_throwsItemServiceException() {
        doThrow(new RuntimeException("Delete error")).when(itemRepository).deleteAll(anyList());

        assertThrows(ItemServiceException.class,
                () -> itemService.delete(Arrays.asList(1L, 2L)));
        verify(itemRepository).deleteAll(anyList());
    }
}
