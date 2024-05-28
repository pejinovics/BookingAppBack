package com.booking.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@MappedSuperclass
public abstract class Person {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    @OneToOne(cascade = { CascadeType.ALL })
    private Address address;

    @Column(nullable = false)
    private String phoneNumber;

    @OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private User user;

    @Column(nullable = false)
    private boolean isNotificationEnabled;
}
