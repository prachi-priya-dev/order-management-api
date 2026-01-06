package com.prachi.order_management_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prachi.order_management_api.domain.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
