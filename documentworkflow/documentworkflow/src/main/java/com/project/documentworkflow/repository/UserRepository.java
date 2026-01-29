package com.project.documentworkflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.documentworkflow.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
