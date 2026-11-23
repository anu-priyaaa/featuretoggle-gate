package com.training.featuretoggle;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Feature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;          // e.g. FARM_ACTIVITY
    private String name;          // shown on screen
    private String moduleName;    // e.g. FARMER_MODULE
    private boolean enabled;      // feature-level switch
}
