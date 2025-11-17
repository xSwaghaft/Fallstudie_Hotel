package com.hotel.booking.service;

import java.util.List;

import com.hotel.booking.entity.Invoice;

public interface InvoiceService {
    List<Invoice> findAll();
    Invoice findById(Long id);
    Invoice create(Invoice invoice);
    Invoice update(Long id, Invoice invoice);
    void delete(Long id);
}
