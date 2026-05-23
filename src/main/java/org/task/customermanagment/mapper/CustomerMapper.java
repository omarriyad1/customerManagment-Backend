package org.task.customermanagment.mapper;

import lombok.Builder;
import org.springframework.stereotype.Component;
import org.task.customermanagment.Dto.CustomerRequestDto;
import org.task.customermanagment.Dto.CustomerResponseDto;
import org.task.customermanagment.Model.Customers;

import java.time.LocalDateTime;

@Component
@Builder
public class CustomerMapper {
    public CustomerResponseDto mapToDTO(Customers customer) {
        return CustomerResponseDto.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .build();
    }


    public Customers mapToEntity(CustomerRequestDto dto) {
        return Customers.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .createdAt(LocalDateTime.now())
                .build();
    }


    public void updateEntityFromDto(CustomerRequestDto dto, Customers existing) {
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
    }
}
