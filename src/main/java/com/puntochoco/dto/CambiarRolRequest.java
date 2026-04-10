package com.puntochoco.dto;

import com.puntochoco.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para cambiar el rol de un usuario")
public class CambiarRolRequest {

    @Schema(description = "Nuevo rol a asignar", example = "SELLER")
    private Rol rol;
}
