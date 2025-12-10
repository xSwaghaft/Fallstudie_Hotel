package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceService {
    List<Invoice> findAll();
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Invoice save(Invoice invoice);
    void deleteById(Long id);
}
