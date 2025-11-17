package com.hotel.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    InvoiceRepository repository;

    @InjectMocks
    InvoiceServiceImpl service;

    private Invoice sample;

    @BeforeEach
    void setup() {
        sample = new Invoice();
        sample.setId(1L);
        sample.setInvoiceNumber("INV-1");
    }

    @Test
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        var got = service.findById(1L);
        assertEquals("INV-1", got.getInvoiceNumber());
    }

    @Test
    void findById_notFound_throws() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(2L));
    }

    @Test
    void create_and_update_and_delete() {
        when(repository.save(any())).thenReturn(sample);
        var created = service.create(sample);
        assertEquals("INV-1", created.getInvoiceNumber());

        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Invoice updated = new Invoice();
        updated.setInvoiceNumber("INV-2");
        var result = service.update(1L, updated);
        assertEquals("INV-2", result.getInvoiceNumber());

        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
