package com.puntochoco.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Roles disponibles en el sistema")
public enum Rol {
    USER,
    SELLER,
    ADMIN
}
