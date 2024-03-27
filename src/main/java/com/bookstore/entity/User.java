package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@Entity
@ToString
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userIdSequence")
    @Column(name = "id", insertable = false, updatable = false)
    @SequenceGenerator(name = "userIdSequence", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Address address;

    @Column(name = "external_id", unique = true, nullable = true)
    private Long externalId;

    private Integer age;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Rating> ratings = new HashSet<>();

    public void setAddress(Address address) {
        address.setUser(this);
        this.address = address;
    }
}
