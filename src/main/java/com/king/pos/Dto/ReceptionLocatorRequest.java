package com.king.pos.Dto;

import java.util.List;

import lombok.Data;

@Data
public class ReceptionLocatorRequest {
        private List<ReceptionLocatorLigneRequest> lignes;

}
