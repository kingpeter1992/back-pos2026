package com.king.pos.Dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ClientDto {
  
    private Long id;
    private String nom;
    private String adresse;
     private String contact;
    private String contact2;
    private String email;
    private String typeClient;
    private boolean actif;
    private  float totalFactures;



    
      public ClientDto() {
    }




      public ClientDto(Long id2, String nom2, String contact3) {
        this.id = id2;
        this.nom = nom2;
        this.contact = contact3;
      
    }




      public ClientDto(Long id2, String nom2, String adresse2, String contact3, String contact22, String email2,
            String typeClient2, boolean actif2) {
              this.id = id2;
              this.nom = nom2;
              this.adresse = adresse2;
              this.contact = contact3;
              this.contact2 = contact22;
              this.email = email2;
              this.typeClient = typeClient2;
              this.actif = actif2;
          
            }

}