package com.rohanswaby.surveyJunky.repository;

import com.rohanswaby.surveyJunky.models.Role;
import com.rohanswaby.surveyJunky.models.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(RoleName role);
}
