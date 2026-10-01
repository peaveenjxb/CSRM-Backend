package com.csrm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "resources")
public class Resource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @NotBlank(message = "Resource name is required.") @Column(nullable = false) public String name;
    @NotBlank(message = "Resource type is required.") @Column(nullable = false) public String type; // CLASSROOM, LAB, LOCKER, EQUIPMENT
    @NotBlank(message = "Location is required.") @Column(nullable = false) public String location;
    public boolean availability = true;

    public Resource() {}
    public Resource(String name, String type, String location) { this.name = name; this.type = type; this.location = location; }
}
