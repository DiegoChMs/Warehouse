package com.diegomorales.warehouse.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity(name = "warehouses_services")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceWarehouse implements Serializable {
    @EmbeddedId
    private ServiceWarehouseId serviceWarehouseId;
}
