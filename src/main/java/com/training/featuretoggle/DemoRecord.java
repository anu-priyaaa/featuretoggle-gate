package com.training.featuretoggle;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/** One row of a catalog feature. Cells are stored joined by "|" (see DemoCatalog). */
@Entity
@Data
public class DemoRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String featureCode;
    @Column(length = 1000)
    private String data;
    private String createdBy;
    private LocalDateTime createdAt;
}
