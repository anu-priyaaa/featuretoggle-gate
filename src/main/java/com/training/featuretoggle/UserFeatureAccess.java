package com.training.featuretoggle;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class UserFeatureAccess {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String featureCode;
    private String grantedBy;
    private LocalDateTime grantedAt;
}