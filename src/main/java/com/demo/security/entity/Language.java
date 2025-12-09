package com.demo.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Language")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Language {
    @Id
    @Column(length = 2)
    private String languageID;

    @Column(nullable = false, length = 20)
    private String language;
}
