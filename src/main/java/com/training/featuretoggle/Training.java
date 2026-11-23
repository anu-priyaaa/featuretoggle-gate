package com.training.featuretoggle;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Training {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String topic;
    private String farmerName;
    private String trainer;
    private LocalDate trainingDate;
    private String status;          // Completed, Scheduled
}