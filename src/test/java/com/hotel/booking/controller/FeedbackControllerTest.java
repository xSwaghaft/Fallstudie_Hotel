package com.hotel.booking.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.service.FeedbackService;

@WebMvcTest(controllers = FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    FeedbackService service;

    @Test
    void getAll_ok() throws Exception {
        Feedback f = new Feedback(); f.setId(1L); f.setRating(4);
        when(service.findAll()).thenReturn(List.of(f));

        mvc.perform(get("/api/feedbacks")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void create_ok() throws Exception {
        Feedback f = new Feedback(); f.setId(2L); f.setRating(5);
        when(service.create(any())).thenReturn(f);

        mvc.perform(post("/api/feedbacks").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(f))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }
}
