package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Role;
import com.moh.vaxtrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);


    List<User> findByRoleOrderByUserIdDesc(Role role);


    List<User> findByRoleAndHospital_DistrictOrderByUserIdDesc(Role role, String district);

}