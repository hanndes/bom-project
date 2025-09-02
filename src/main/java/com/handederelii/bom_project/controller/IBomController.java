package com.handederelii.bom_project.controller;

import com.handederelii.bom_project.dto.request.BomRequest;
import com.handederelii.bom_project.dto.response.BomResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

public interface IBomController {
    public BomResponse query(@Valid @RequestBody BomRequest req, Principal principal) ;
}
