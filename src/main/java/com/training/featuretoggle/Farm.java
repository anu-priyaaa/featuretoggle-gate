package com.training.featuretoggle;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Farm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String farmerName;
    private String village;
    private String crop;
    private double areaAcres;
    private String activity;        // Sowing, Irrigation, Pest spray, Harvest
    private LocalDate activityDate;
}