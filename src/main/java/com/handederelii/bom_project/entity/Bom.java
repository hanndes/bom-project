package com.handederelii.bom_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "bom",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_bom_triple",
                        columnNames = {"scope","actor_id","body_hash"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String scope;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "body_hash", nullable = false, length = 64)
    private String bodyHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BomStatus status; // PENDING/DONE/FAILED

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}