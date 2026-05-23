package org.task.customermanagment.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.task.customermanagment.Model.Customers;

import java.util.List;
import java.util.Optional;

@Repository
public interface customerRepository extends JpaRepository<Customers, Integer> {
    boolean existsByEmail(String email);

    Page<Customers> findAll(Pageable pageable);

    Page<Customers> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Customers> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Customers> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );
}
