package com.mfgops.workforce.shift;

import com.mfgops.common.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "shifts")
public class Shift extends TenantBaseEntity {

    @Column(name = "name", nullable = false)
    private String name; // MORNING, EVENING, NIGHT

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
