package com.training.featuretoggle;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Dispatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String product;
    private int quantityKg;
    private String destination;
    private String status;          // Dispatched, In transit, Delivered
    private LocalDate dispatchDate;
}