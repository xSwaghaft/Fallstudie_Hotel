package com.hotel.booking.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.InvoiceRepository;
import com.hotel.booking.service.InvoiceService;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;

    public InvoiceServiceImpl(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Invoice> findAll() {
        return repository.findAll();
    }

    @Override
    public Invoice findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    @Override
    public Invoice create(Invoice invoice) {
        return repository.save(invoice);
    }

    @Override
    public Invoice update(Long id, Invoice invoice) {
        Invoice existing = findById(id);
        existing.setInvoiceNumber(invoice.getInvoiceNumber());
        existing.setAmount(invoice.getAmount());
        existing.setPaidAt(invoice.getPaidAt());
        existing.setPaymentMethod(invoice.getPaymentMethod());
        existing.setInvoiceStatus(invoice.getInvoiceStatus());
        existing.setBooking(invoice.getBooking());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice", "id", id);
        }
        repository.deleteById(id);
    }
}
