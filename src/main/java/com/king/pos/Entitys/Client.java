package com.king.pos.Entitys;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    private Long id;
    private String nom;
    private String adresse;
    private String contact;
    private String contact2;
    private String email;
    private String typeClient;
    private boolean actif = true;

}
