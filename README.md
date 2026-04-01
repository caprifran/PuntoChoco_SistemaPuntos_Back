# PuntoChoco - Sistema de Puntos 🍫

Backend de un sistema de gestión de puntos de fidelización para **Punto Chocolate**. Permite administrar clientes, acumular/canjear puntos, gestionar productos canjeables y controlar usuarios con roles diferenciados.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Security + JWT | jjwt 0.12.6 |
| Spring Data JPA / Hibernate | — |
| PostgreSQL (Supabase) | — |
| Maven | 3.9 |
| Docker & Docker Compose | 3.9 |
| Lombok | — |

## Arquitectura

```
backend/src/main/java/com/puntochoco/
├── config/          # Configuración de seguridad y CORS
├── controller/      # Controladores REST
├── dto/             # Objetos de transferencia de datos
├── model/           # Entidades JPA (Cliente, Movimiento, Producto, Usuario, Rol)
├── repository/      # Repositorios Spring Data
├── security/        # Filtro JWT, UserDetailsService, utilidades JWT
└── service/         # Lógica de negocio
```

## Funcionalidades principales

- **Autenticación y autorización** con JWT y roles (`ADMIN`, `SELLER`, `USER`)
- **Gestión de clientes**: alta, baja lógica, búsqueda y consulta de puntos
- **Sistema de puntos**: acumulación, canje por productos, historial de movimientos
- **Gestión de productos**: catálogo de premios canjeables con costo en puntos
- **Gestión de usuarios**: asignación de roles, activación/desactivación de cuentas
- **Reportes**: ranking de clientes por puntos y consumo, historial con filtros por fecha
- **Health check** para monitoreo del servicio

## Endpoints de la API

### Auth (`/api/auth`) — Público
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión y obtener token JWT |
| POST | `/api/auth/register` | Registrar nuevo usuario |

### Clientes (`/api/clientes`) — ADMIN / SELLER
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/clientes` | Listar clientes activos (con búsqueda opcional) |
| GET | `/api/clientes/top` | Ranking de clientes por puntos o consumo |
| GET | `/api/clientes/historico` | Historial de movimientos (filtro por fecha) |
| GET | `/api/clientes/{id}` | Detalle de cliente con puntos totales |
| POST | `/api/clientes` | Crear cliente |
| PUT | `/api/clientes/{id}` | Actualizar cliente |
| PUT | `/api/clientes/{id}/bajaCliente` | Baja lógica de cliente |
| PUT | `/api/clientes/{id}/agregarPuntos` | Agregar puntos |
| PUT | `/api/clientes/{id}/descontarPuntos` | Canjear puntos por producto |

### Productos (`/api/productos`) — ADMIN / SELLER
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/productos` | Listar productos activos (con búsqueda opcional) |
| GET | `/api/productos/{id}` | Detalle de producto |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| PUT | `/api/productos/{id}/bajaProducto` | Baja lógica de producto |

### Usuarios (`/api/usuarios`) — ADMIN
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Listar usuarios (con búsqueda opcional) |
| GET | `/api/usuarios/{id}` | Detalle de usuario |
| PUT | `/api/usuarios/{id}/rol` | Cambiar rol de usuario |
| PUT | `/api/usuarios/{id}/activar` | Activar cuenta |
| PUT | `/api/usuarios/{id}/desactivar` | Desactivar cuenta |

### Health (`/api`) — Público
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/health` | Verificar estado del servicio |

## Requisitos previos

- [Docker](https://www.docker.com/) y Docker Compose instalados
- Archivo `.env` en la raíz del proyecto con las variables de entorno necesarias

### Variables de entorno

Crear un archivo `.env` en la raíz con el siguiente formato:

```env
BACKEND_PORT=4000
DB_HOST=tu-host-postgresql
DB_PORT=6543
DB_USER=tu-usuario
DB_PASSWORD=tu-contraseña
DB_NAME=tu-base-de-datos
JWT_SECRET=tu-clave-secreta-jwt-de-al-menos-256-bits
JWT_EXPIRATION=86400000
```

## Instalación y ejecución

### Con Docker (recomendado)

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/puntochoco-backend.git
cd puntochoco-backend

# Crear archivo .env con las variables necesarias

# Construir y levantar contenedores
docker-compose build
docker-compose up -d
```

### Sin Docker (desarrollo local)

```bash
cd backend

# Definir variables de entorno o usar application-local.properties

mvn spring-boot:run
```

La API estará disponible en `http://localhost:4000`.

## Scripts de utilidad

| Script | Descripción |
|---|---|
| `levantar_proyecto.bat` | Verifica Docker, respalda la BD opcionalmente y levanta los contenedores |
| `restauracion-bd.bat` | Lista backups SQL disponibles y restaura el seleccionado |

## Modelo de datos

```
Cliente (id, nombre, apellido, dni, fechaAlta, fechaBaja)
    └── Movimiento (id, tipo, cantidad, fechaCreacion, fechaVencimiento, producto)
Producto (id, descripcion, puntos, imagenUrl, fechaAlta, fechaBaja)
Usuario (id, nombre, email, password, rol, activo)
Rol (ADMIN, SELLER, USER)
```

## Licencia

Este proyecto es parte de un portfolio personal.