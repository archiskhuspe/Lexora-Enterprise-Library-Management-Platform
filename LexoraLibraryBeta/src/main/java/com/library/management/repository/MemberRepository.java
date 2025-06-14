package com.library.management.repository;

import com.library.management.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMembershipId(String membershipId);
    boolean existsByEmail(String email);
    boolean existsByMembershipId(String membershipId);
    Page<Member> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Member> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Member> findByPhoneNumberContainingIgnoreCase(String phoneNumber, Pageable pageable);
    Page<Member> findByMembershipIdContainingIgnoreCase(String membershipId, Pageable pageable);
    Page<Member> findByActive(boolean active, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.member.id = ?1 AND l.status = 'ACTIVE'")
    int countActiveLoans(Long memberId);

    @Query("SELECT m FROM Member m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(m.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:membershipId IS NULL OR m.membershipId = :membershipId) AND " +
           "(:phoneNumber IS NULL OR m.phoneNumber = :phoneNumber) AND " +
           "(:active IS NULL OR m.active = :active)")
    Page<Member> searchMembers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("membershipId") String membershipId,
            @Param("phoneNumber") String phoneNumber,
            @Param("active") Boolean active,
            Pageable pageable);
} 