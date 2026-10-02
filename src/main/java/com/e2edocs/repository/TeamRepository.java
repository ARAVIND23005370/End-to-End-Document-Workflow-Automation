package com.e2edocs.repository;

import com.e2edocs.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    List<Team> findByOrganizationId(String organizationId);
    List<Team> findByDepartmentId(String departmentId);
}
