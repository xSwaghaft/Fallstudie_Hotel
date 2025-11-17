package com.hotel.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    PaymentRepository repository;

    @InjectMocks
    PaymentServiceImpl service;

    private Payment sample;

    @BeforeEach
    void setup() {
        sample = new Payment();
        sample.setId(1L);
        sample.setAmount(new BigDecimal("10.00"));
    }

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(sample));
        var result = service.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        var p = service.findById(1L);
        assertEquals(sample.getId(), p.getId());
    }

    @Test
    void findById_notFound_throws() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(2L));
    }

    @Test
    void create_saves() {
        when(repository.save(any())).thenReturn(sample);
        var created = service.create(sample);
        assertEquals(sample.getId(), created.getId());
    }

    @Test
    void update_existing_updatesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment updated = new Payment();
        updated.setAmount(new BigDecimal("20.00"));

        var result = service.update(1L, updated);
        assertEquals(new BigDecimal("20.00"), result.getAmount());
    }

    @Test
    void delete_missing_throws() {
        when(repository.existsById(5L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
    }

    @Test
    void delete_existing_callsRepository() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
