package com.training.featuretoggle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    // Search by email with paging (empty text returns everyone)
    Page<AppUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Users who have been granted at least one of the given features
    @Query("select u from AppUser u "
            + "where lower(u.email) like lower(concat('%', :q, '%')) "
            + "and u.id in (select a.userId from UserFeatureAccess a where a.featureCode in :codes)")
    Page<AppUser> searchWithAccess(@Param("q") String q,
                                   @Param("codes") Collection<String> codes,
                                   Pageable pageable);
}