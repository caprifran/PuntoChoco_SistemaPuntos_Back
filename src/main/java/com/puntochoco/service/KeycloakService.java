package com.puntochoco.service;


import com.puntochoco.repository.UsuarioRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakService {

    private final Keycloak keycloak;
    private final UsuarioRepository usuarioRepository;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakService(Keycloak keycloak, UsuarioRepository usuarioRepository) {
        this.keycloak = keycloak;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un usuario en Keycloak y asigna su rol inicial.
     * @return El ID (sub) de Keycloak del usuario creado.
     */
    public String crearUsuario(com.puntochoco.dto.RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getNombre());
        user.setLastName(request.getApellido());
        user.setEmailVerified(true);

        // Crear el usuario
        jakarta.ws.rs.core.Response response = keycloak.realm(realm).users().create(user);
        
        if (response.getStatus() != 201) {
            String errorMsg = response.readEntity(String.class);
            throw new RuntimeException("Error al crear usuario en Keycloak (Status " + response.getStatus() + "): " + errorMsg);
        }

        // Obtener el ID del usuario creado desde el header Location
        if (response.getLocation() == null) {
            // Fallback: buscar por username si el Location es nulo
            List<UserRepresentation> found = keycloak.realm(realm).users().search(request.getUsername(), true);
            if (found.isEmpty()) throw new RuntimeException("Usuario creado en Keycloak pero no se pudo recuperar su ID");
            return found.get(0).getId();
        }

        String path = response.getLocation().getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);

        if (userId == null || userId.isBlank()) {
             throw new RuntimeException("No se pudo extraer el ID del usuario del header Location: " + path);
        }

        // Configurar la contraseña
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());

        keycloak.realm(realm).users().get(userId).resetPassword(credential);

        // Asignar rol si se especificó
        if (request.getRol() != null && !request.getRol().isBlank()) {
            asignarRol(userId, request.getRol());
        }

        return userId;
    }

    /**
     * Habilita o deshabilita un usuario en Keycloak y sincroniza el estado en la DB local.
     */
    public void setUsuarioActivo(String keycloakId, boolean activo) {
        UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
        UserRepresentation userRep = userResource.toRepresentation();
        userRep.setEnabled(activo);
        userResource.update(userRep);

        // Sincronizar localmente
        usuarioRepository.findById(keycloakId).ifPresent(u -> {
            u.setActivo(activo);
            usuarioRepository.save(u);
        });
    }

    /**
     * Asigna un rol de realm a un usuario en Keycloak.
     * Elimina los roles anteriores (asumiendo que la lógica de la app maneja un solo rol principal).
     */
    public void asignarRol(String keycloakId, String roleName) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            
            // 1. Verificar si el rol existe en Keycloak
            RoleRepresentation newRole;
            try {
                newRole = keycloak.realm(realm).roles().get(roleName.toUpperCase()).toRepresentation();
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new RuntimeException("El rol '" + roleName.toUpperCase() + "' no existe en Keycloak. Por favor, créalo en 'Realm Roles'.");
            } catch (Exception e) {
                throw new RuntimeException("Error al buscar el rol '" + roleName + "' en Keycloak: " + e.getMessage());
            }

            // 2. Obtener roles actuales del realm y removerlos (para que tenga solo uno)
            // Solo removemos roles que no sean default de Keycloak (opcional, pero más seguro)
            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();
            if (currentRoles != null && !currentRoles.isEmpty()) {
                userResource.roles().realmLevel().remove(currentRoles);
            }

            // 3. Asignar el nuevo rol
            userResource.roles().realmLevel().add(Collections.singletonList(newRole));
        } catch (Exception e) {
            throw new RuntimeException("Falla al asignar rol en Keycloak: " + e.getMessage());
        }
    }

    /**
     * Obtiene los roles de Keycloak para un usuario específico.
     */
    public List<String> getRoles(String keycloakId) {
        return keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por username y retorna su ID.
     * Retorna null si no lo encuentra.
     */
    public String getUserIdByUsername(String username) {
        List<UserRepresentation> found = keycloak.realm(realm).users().search(username, true);
        if (found != null && !found.isEmpty()) {
            return found.get(0).getId();
        }
        return null;
    }

    /**
     * Retorna todos los usuarios del realm.
     */
    public List<UserRepresentation> listarTodosLosUsuarios() {
        return keycloak.realm(realm).users().list();
    }
}
