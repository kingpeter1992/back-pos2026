package com.king.pos.Interface;

import java.util.List;

import com.king.pos.Dto.Response.SuggestionApproResponse;

public interface ApprovisionnementService {
    
        List<SuggestionApproResponse> getSuggestionsReapprovisionnement(Integer joursCouverture);

}
