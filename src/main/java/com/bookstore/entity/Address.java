package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "user")
@Getter
@Builder
@ToString(exclude = "user")
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String city;

    private String region;

    private String country;
}
