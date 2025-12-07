package com.hotel.booking.service.impl;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.repository.InvoiceRepository;
import com.hotel.booking.service.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

	private final InvoiceRepository invoiceRepository;

	public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Invoice> findAll() {
		return invoiceRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Invoice> findById(Long id) {
		return invoiceRepository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
		return invoiceRepository.findByInvoiceNumber(invoiceNumber);
	}

	@Override
	public Invoice save(Invoice invoice) {
		return invoiceRepository.save(invoice);
	}

	@Override
	public void deleteById(Long id) {
		invoiceRepository.deleteById(id);
	}
}

