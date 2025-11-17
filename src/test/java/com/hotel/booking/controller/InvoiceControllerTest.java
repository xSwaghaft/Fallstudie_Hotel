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

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.service.InvoiceService;

@WebMvcTest(controllers = InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    InvoiceService service;

    @Test
    void getAll_ok() throws Exception {
        Invoice i = new Invoice(); i.setId(1L); i.setInvoiceNumber("INV-1");
        when(service.findAll()).thenReturn(List.of(i));

        mvc.perform(get("/api/invoices")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void create_returnsCreated() throws Exception {
        Invoice i = new Invoice(); i.setId(7L); i.setInvoiceNumber("INV-7");
        i.setAmount(new BigDecimal("123.45"));
        i.setPaymentMethod(PaymentMethod.CARD);
        when(service.create(any())).thenReturn(i);

        mvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(i))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7));
    }
}
