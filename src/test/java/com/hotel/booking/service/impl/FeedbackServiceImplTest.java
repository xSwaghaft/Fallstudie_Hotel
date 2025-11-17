package com.hotel.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.FeedbackRepository;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    FeedbackRepository repository;

    @InjectMocks
    FeedbackServiceImpl service;

    private Feedback sample;

    @BeforeEach
    void setup() {
        sample = new Feedback();
        sample.setId(1L);
        sample.setRating(4);
    }

    @Test
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        var got = service.findById(1L);
        assertEquals(4, got.getRating());
    }

    @Test
    void findById_notFound_throws() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(2L));
    }

    @Test
    void create_update_delete_flow() {
        when(repository.save(any())).thenReturn(sample);
        var created = service.create(sample);
        assertEquals(1L, created.getId());

        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Feedback updated = new Feedback();
        updated.setRating(5);
        var res = service.update(1L, updated);
        assertEquals(5, res.getRating());

        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
