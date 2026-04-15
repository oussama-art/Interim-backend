package com.TroisN.Service.event;

import com.TroisN.Service.entity.Assignment;

import java.util.List;

public record DemandeDeletedEvent(
        Long demandeId,
        List<Assignment> assignments
) {}