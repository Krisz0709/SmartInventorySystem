package hu.smartinventory.user.repository;

import hu.smartinventory.user.entity.UserRole;
import hu.smartinventory.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    @Query("""
            SELECT ur
            FROM UserRole ur
            JOIN FETCH ur.role
            WHERE ur.user.id = :userId
            ORDER BY ur.role.id
            """)
    List<UserRole> findAllByUserIdWithRole(
            @Param("userId") Long userId
    );
}