package com.e2edocs.repository;

import com.e2edocs.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByOrganizationIdOrderByTimestampDesc(String organizationId);
    List<Notification> findByOrganizationIdAndUserIdOrderByTimestampDesc(String organizationId, String userId);
    
    long countByOrganizationIdAndReadFalse(String organizationId);
    long countByOrganizationIdAndUserIdAndReadFalse(String organizationId, String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.organizationId = :organizationId")
    void markAllAsReadForOrg(String organizationId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.organizationId = :organizationId AND (n.userId = :userId OR n.userId IS NULL)")
    void markAllAsReadForUser(String organizationId, String userId);
}
