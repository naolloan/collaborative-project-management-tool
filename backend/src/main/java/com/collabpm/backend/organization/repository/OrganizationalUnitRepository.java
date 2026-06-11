package com.collabpm.backend.organization.repository;

import com.collabpm.backend.organization.model.OrganizationalUnit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, Long> {

    List<OrganizationalUnit> findAllByActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
