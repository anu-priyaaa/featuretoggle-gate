package com.training.featuretoggle;



import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class FeatureFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;          // e.g. FARMER_MODULE

    private String description;   // shown on screen
    private boolean enabled;      // the switch itself
    private String updatedBy;     // who flipped it last
    private LocalDateTime updatedAt;
}