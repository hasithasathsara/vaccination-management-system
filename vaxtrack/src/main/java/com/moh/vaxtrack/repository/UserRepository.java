package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Role;
import com.moh.vaxtrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by Spring Security during login to look up whoever's typing in the username field.
    Optional<User> findByUsername(String username);

    // Used by "Add Sub-Admin" / "Add Inventory Manager" forms to block duplicate usernames
    // before we even try to save (friendlier than waiting for a database constraint error).
    boolean existsByUsername(String username);

    // "SELECT * FROM user WHERE role = ? ORDER BY user_id DESC"
    // Reused by every role-specific management page: Sub Admin Management, Inventory Staff
    // Management, and (within a specific hospital) the Nurse list on the Sub-Admin dashboard.
    List<User> findByRoleOrderByUserIdDesc(Role role);

}
