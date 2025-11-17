package com.hotel.booking.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import com.hotel.booking.entity.PaymentMethod;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.service.PaymentService;

@WebMvcTest(controllers = PaymentController.class)
class PaymentControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    PaymentService service;

    @Test
    void getAll_returnsOk() throws Exception {
        Payment p = new Payment(); p.setId(1L); p.setAmount(new BigDecimal("12.00"));
        when(service.findAll()).thenReturn(List.of(p));

        mvc.perform(get("/api/payments")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getById_returnsPayment() throws Exception {
        Payment p = new Payment(); p.setId(2L); p.setAmount(new BigDecimal("5.00"));
        when(service.findById(2L)).thenReturn(p);

        mvc.perform(get("/api/payments/2")).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void create_returnsCreated() throws Exception {
        Payment p = new Payment(); p.setId(3L); p.setAmount(new BigDecimal("7.00"));
        p.setMethod(PaymentMethod.CARD);
        when(service.create(any())).thenReturn(p);

        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(p)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        doNothing().when(service).delete(4L);
        mvc.perform(delete("/api/payments/4")).andExpect(status().isNoContent());
    }
}
