package org.task.customermanagment.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.task.customermanagment.Exception.CustomerAlreadyExists;
import org.task.customermanagment.mapper.CustomerMapper;
import org.task.customermanagment.Dto.CustomerRequestDto;
import org.task.customermanagment.Dto.CustomerResponseDto;
import org.task.customermanagment.Exception.NoCustomersFound;
import org.task.customermanagment.Model.Customers;
import org.task.customermanagment.Repository.customerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class customerService {

    private final customerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponseDto> getAllCustomers() {

        List<Customers> customers = customerRepository.findAll();

        if (customers.isEmpty()) {
            throw new NoCustomersFound("No Customers found");
        }

        return customers.stream()
                .map(customer -> new CustomerResponseDto(
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getCreatedAt()
                ))
                .toList();
    }

    public CustomerResponseDto getCustomerById(int id) {
        log.debug("Fetching customer with id: {}", id);

        Customers customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Customer not found with id: {}", id);
                    return new NoCustomersFound("Customer with id " + id + " doesn't exist");
                });

        CustomerResponseDto dto = customerMapper.mapToDTO(customer);
        log.debug("Returning customer: {}", dto);
        return dto;
    }

    public CustomerResponseDto createCustomer(CustomerRequestDto requestDto) {
        log.debug("Creating customer with email: {}", requestDto.getEmail());

        if (customerRepository.existsByEmail(requestDto.getEmail())) {
            log.debug("Customer already exists with email: {}", requestDto.getEmail());
            throw new CustomerAlreadyExists(
                    "Customer with email " + requestDto.getEmail() + " already exists"
            );
        }

        Customers customer = customerMapper.mapToEntity(requestDto);
        Customers saved = customerRepository.save(customer);

        log.info("Customer created with id: {}", saved.getId());
        return customerMapper.mapToDTO(saved);
    }

    @Transactional
    public CustomerResponseDto updateCustomer(int id, CustomerRequestDto requestDto) {
        log.debug("Updating customer with id: {}", id);

        Customers existing = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Customer not found for update with id: {}", id);
                    return new NoCustomersFound("Customer with id " + id + " doesn't exist");
                });

        if (!existing.getEmail().equalsIgnoreCase(requestDto.getEmail())
                && customerRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomerAlreadyExists(
                    "Email " + requestDto.getEmail() + " is already taken"
            );
        }

        existing.setName(requestDto.getName());
        existing.setEmail(requestDto.getEmail());
        existing.setPhone(requestDto.getPhone());

        Customers updated = customerRepository.save(existing);

        log.info("Customer updated with id: {}", updated.getId());
        return customerMapper.mapToDTO(updated);
    }


    @Transactional
    public void deleteCustomer(int id) {
        log.debug("Deleting customer with id: {}", id);

        if (!customerRepository.existsById(id)) {
            log.debug("Customer not found for deletion with id: {}", id);
            throw new NoCustomersFound("Customer with id " + id + " doesn't exist");
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted with id: {}", id);
    }



    public Page<CustomerResponseDto> getPaginatedCustomers(
            String search,
            Pageable pageable
    ) {

        Page<Customers> customersPage;

        if (search != null && !search.isBlank()) {
            customersPage = customerRepository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search, search, pageable
                    );
        } else {
            customersPage = customerRepository.findAll(pageable);
        }

        return customersPage.map(customerMapper::mapToDTO);
    }
}
