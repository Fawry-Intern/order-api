package com.fawry.order_api.infrastructure.repository;

import com.fawry.order_api.domain.model.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    Optional<List<Outbox>> findTop100ByProcessed(Boolean processed);
}
