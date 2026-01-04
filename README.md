# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Requerimientos para ejecutar el backend
- Docker instalado y corriendo.
- Acceso a la base de datos remota configurada vía variables de entorno (por defecto: `DB_HOST=gondola.proxy.rlwy.net`, `DB_PORT=39683`, `DB_NAME=railway`, `DB_SCHEMA=public`, `DB_USER=postgres`, `DB_PASSWORD=KlCadXcunKSraYhTlNuzXcWNBOkhscWD`).

## Levantar la aplicación en Docker (build incluido)
Desde la raíz del proyecto:
```
cd "C:\\Users\\yerid\\Documents\\Proyectos\\prueba tecnica 2"
docker build -f deployment/Dockerfile -t prueba-franquicia-app .
docker run -d --name prueba-franquicia -p 8082:8082 ^
  -e DB_HOST=gondola.proxy.rlwy.net ^
  -e DB_PORT=39683 ^
  -e DB_NAME=railway ^
  -e DB_SCHEMA=public ^
  -e DB_USER=postgres ^
  -e DB_PASSWORD=KlCadXcunKSraYhTlNuzXcWNBOkhscWD ^
  prueba-franquicia-app
```
El servicio queda disponible en `http://localhost:8082`.

## Rutas expuestas (API REST)
- Franquicias:
  - `GET /api/franquicias` listar todas.
  - `GET /api/franquicias/contar` total de franquicias.
  - `GET /api/franquicias/{id}` obtener por id.
  - `GET /api/franquicias/nombre/{nombre}` obtener por nombre exacto.
  - `GET /api/franquicias/buscar/{nombre}` buscar por nombre (contiene).
  - `POST /api/franquicias` crear.
  - `PUT /api/franquicias/{id}` actualizar.
  - `DELETE /api/franquicias/{id}` eliminar por id.
  - `DELETE /api/franquicias/nombre/{nombre}` eliminar por nombre.

- Sucursales:
  - `GET /api/sucursales` listar todas.
  - `GET /api/sucursales/{id}` obtener por id.
  - `GET /api/sucursales/nombre/{nombre}` obtener por nombre exacto.
  - `GET /api/sucursales/buscar/{nombre}` buscar por nombre (contiene).
  - `GET /api/franquicias/{franquiciaId}/sucursales` listar por franquicia.
  - `GET /api/franquicias/{franquiciaId}/sucursales/contar` total por franquicia.
  - `POST /api/franquicias/{franquiciaId}/sucursales` crear sucursal.
  - `PUT /api/sucursales/{id}` actualizar.
  - `DELETE /api/sucursales/{id}` eliminar.

- Productos:
  - `GET /api/productos` listar todos.
  - `GET /api/productos/{id}` obtener por id.
  - `GET /api/productos/nombre/{nombre}` obtener por nombre exacto.
  - `GET /api/productos/stock-bajo/{stock}` listar con stock menor al valor.
  - `GET /api/sucursales/{sucursalId}/productos` listar por sucursal.
  - `GET /api/sucursales/{sucursalId}/productos/buscar/{nombre}` buscar por nombre en una sucursal.
  - `GET /api/sucursales/{sucursalId}/productos/mas-caro` obtener producto más caro de la sucursal.
  - `GET /api/sucursales/{sucursalId}/productos/contar` total por sucursal.
  - `POST /api/sucursales/{sucursalId}/productos` crear producto.
  - `PUT /api/productos/{id}` actualizar.
  - `DELETE /api/productos/{id}` eliminar.

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**
