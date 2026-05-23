package org.task.customermanagment.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.task.customermanagment.Model.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}