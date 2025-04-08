package com.fawry.order_api.infrastructure.repository;

import com.fawry.order_api.domain.model.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    Optional<List<Outbox>> findTop10ByProcessed(Boolean processed);

    @Modifying
    @Query("""
            UPDATE Outbox o SET o.processed = :outboxProcessing WHERE o.id IN :ids
            """)
    void updateProcessedByIds(@Param("outboxProcessing") Boolean processed, @Param("ids") List<Long> outboxIds);
}
