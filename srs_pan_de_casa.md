# Documento de Especificación Técnica de Software (SRS) - Pan de Casa (Versión Definitiva 2.0)

Este documento representa la Ingeniería Inversa exhaustiva, verificada y auditada del sistema "Pan de Casa". Contiene la arquitectura lógica, el sistema de diseño, modelos de datos, flujos de usuario completos, y la infraestructura de calidad y estándares (ISO) subyacentes. Su propósito es servir como única fuente de la verdad para reconstruir la plataforma (P. ej., a un desarrollo móvil nativo o refactorización de framework moderno).

---

## 1. Mapa de Arquitectura de Información

El proyecto diferencia lógicamente dos divisiones principales, sin depender de un enrutador clásico de Single Page Application, utilizando una navegación estricta por archivos `.html`.

### 1.1 Vistas de Cliente (Tienda)
- **Catálogo Principal (`index.html`)**
  - **Jerarquía:** Raíz de acceso del cliente.
  - **Navegación Saliente:** 
    - Nav-Bar: "Mis Pedidos", "Admin", Link icono "Carrito".
    - Interacción: Botón "Agregar" (Añade al carrito en background).
  - **Estructura Interna:** Hero de Bienvenida, Cuadrícula de inyección dinámica para tarjetas de producto (`<div id="productGrid">`).
- **Carrito de Compras (`cart.html`)**
  - **Jerarquía:** Segundo nivel.
  - **Navegación Saliente:** 
    - Botones principales: "Proceder al Pago" (dirige a Checkout).
    - Botones de acción vacía: "Ir al Catálogo", "Vaciar Carrito".
  - **Estructura Interna:** Vista dinámica de estado "Vacío" si `Cart.length == 0`. Layout en grilla asimétrica (Lista de ítems interactiva / Cuadro de Resumen lateral).
- **Finalizar Compra - Checkout (`checkout.html`)**
  - **Jerarquía:** Tercer nivel de conversión.
  - **Navegación Saliente:** "Confirmar Pedido" (Submit de formulario dirige a Confirmación).
  - **Estructura Interna:** Formulario maestro capturando `CustomerData` y resumen inamovible de la orden activa pre-generada.
- **Pedido Confirmado (`confirmation.html`)**
  - **Jerarquía:** Fin de túnel transaccional (Página de destino tras compra exitosa).
  - **Navegación Saliente:** "Rastrear Pedido" (hacia Order Status), "Volver al Inicio".
  - **Estructura Interna:** Receptor URL de `?orderId=...` visible destacada. Iconografía de triunfo y recuento de los pasos posteriores.
- **Estado de mi Pedido (`order-status.html`)**
  - **Jerarquía:** Área de retención de usuario (Post-venta).
  - **Navegación Saliente:** "Ir al Catálogo" (si no hay órdenes).
  - **Estructura Interna:** Contenedores de Tarjeta `tracker-card`. Barras de Progreso calculadas matemáticamente e impulsivamente re-renderizadas cada 5000ms.

### 1.2 Vistas del Administrador
- **Login Administrativo (`admin/login.html`)**
  - **Jerarquía:** Barrera de entrada para backoffice.
  - **Navegación Saliente:** "Ingresar al Panel" (Submit dirige al dashboard), "Volver a la Tienda".
- **Dashboard (`admin/dashboard.html`)**
  - **Jerarquía:** Raíz Backoffice.
  - **Navegación Saliente:** Sidebar global: Dashboard, Pedidos Activos, Catálogo, Volver a Tienda, Cerrar Sesión.
  - **Estructura Interna:** Contenedores de Resumen Métrico (Total Ventas, Conteo) y Tabla de Producción consolidada de sumatorias.
- **Gestión de Pedidos (`admin/orders.html`)**
  - **Jerarquía:** Tabla Interactiva Operativa.
  - **Estructura Interna:** Tabla dinámica con acciones mutables para el progreso del flujo logístico por iteración atómica.
- **Gestión de Catálogo (`admin/product-form.html`)**
  - **Jerarquía:** Gestor CRUD de Entidades.
  - **Estructura Interna:** Layout bipartito: Formulario Unificado (Crear/Editar interceptable por identificador oculto) y Lista renderizada con acciones "Editar" y "Borrar".

### 1.3 Estructura Visual de Interfaces (Wireframes / Mockups Lógicos)
Para la reconstrucción del cliente (Ej: App Móvil o web moderna), estos son los Layouts visuales detectados en las plantillas HTML (Mockups estructurales):
- **Mockup `index.html`:** Cabecera adhesiva superior (Logo Izquierda, Links y Badge Carrito a la Derecha). Bloque "Hero" principal centrado con degradado suave. Debajo, Grilla dinámica responsiva de 1 a 4 columnas con Tarjetas de Producto.
- **Mockup `cart.html`:** Cabecera estándar. Título h1. Layout dividido en Desktop (Grid 2:1): Columna izquierda ancha con Lista vertical de elementos en bloque horizontal (Foto, nombre, Precio, Botones `+`/`-`). Columna derecha más estrecha con un Cúbico Flotante fijo (Sticky) mostrando el Resumen, Envío, Total y botón "Proceder al Pago".
- **Mockup `checkout.html`:** Layout dividido estilo Cart (2:1). Izquierda: Agrupación en 3 Bloques (Datos de Contacto, Detalles de Entrega, Método de Pago). Derecha: Resumen de Orden bloqueada estáticamente.
- **Mockup `order-status.html`:** Tarjeta central expansible con el ID del Pedido y Total en el extremo superior. Debajo, un Progress Bar horizontal de 4 Pasos (`Recibido`, `En Preparación`, `Listo`, `Entregado`) unido por una barra de fondo cruzada transversal. Segmento inferior lista la compra condensada.
- **Mockup *Admin Panel*:** Layout de Panel de Control clásico. Menú Sidebar fijo a la Izquierda (260px ancho en Desktop, Colapsable hacia arriba en Móvil). El Espacio sobrante derecho renderiza el Contenido Principal (`admin-content`) usando tarjetas blancas apiladas y tablas de registro.
---

## 2. Infraestructura Técnica Oculta y Normativa (ISO Compliance)

Aunque el front se apoya en Vanilla JS y Vanilla CSS, el repositorio (`package.json`) y el (`README.md`) dictaminan una infraestructura de estándares de desarrollo y control.

### 2.1 Tooling y Configuración (Node.js)
El proyecto usa el motor Node/NPM exclusivo para la calidad de Integración Continua (devTools):
- **ESLint:** Controla la sintaxis JS (bajo configuraciones `eslint.config.js`). Garantiza código limpio bajo ISO 25000 de mantenibilidad.
- **Stylelint:** Linter estricto de estilos bajo el estándar universal (`stylelint-config-standard`).

### 2.2 Requerimientos ISO Implementados
La reconstrucción móvil debe emular estas mitigaciones exactas:
- **ISO 5055 & ISO 27001 (Seguridad y Fiabilidad):** Exige mitigación nativa a **Inyección de Código XSS (Cross-Site Scripting)**. Al inyectar variables en UI procedentes de la base de datos local previene ejecuciones indeseadas escapando los string con `window.escapeHTML()`.
- **ISO 25000 (Calidad y Mantenibilidad):** Requiere un Patrón "Try/Catch" de Seguridad Persistente en torno a fallos del origen y un patrón arquitectónico Event-Driven (Pub/Sub) para comunicación UI/Capa de datos.

---

## 3. Diccionario de Lógica de Negocio (`JS`)

### 3.1 Utilidades y Helpers Centrales (`store.js`)
| Función | Entradas y Datos | Lógica Interna | Resultado (Salida) | Validaciones Críticas |
| --- | --- | --- | --- | --- |
| `init()` | `None` | Si el LocalStorage no posee llaves de Sistema, inyecta `pandecasa_...(products, cart, orders)`. Pre-carga un Array estático del catálogo origen si Product es Null. | Carga en RAM/Disk Storage. Modifica Storage Global. | Patrón `try-catch` aislando la aplicación si LocalStorage está deshabilitado en el navegador. |
| `formatCurrency()` | `value` (Number) | `Intl.NumberFormat` en modo `es-MX`. | (String) Formato de Pesos `$ 85.00` | N/A |
| `formatDate()` | `dateString` (Miliseconds/String) | `Intl.DateTimeFormat` (medium date, short time) para enmascaramiento MX. | (String) Formato Fecha `20 Mar, 10:00` | N/A |
| `escapeHTML()` | `str` (String) | Expresión Regular Reemplazo `/[&<>'"]/g` transformando los tag HTML en Texto String nativo. | (String) texto Escapado y sanitizado. | Si `typeof !== 'string'`, devuelve el elemento inmutable. ISO 27001 vital. |

### 3.2 Dominio: Manejo de Productos (CRUD Catalogo)
Todas pertecenen a la clase Singleton `Store` instanciada en `window.AppStore`

| Función | Entradas y Datos | Lógica Interna | Resultado (Salida) |
| --- | --- | --- | --- |
| `getProducts()` | `None` | Retorna el listado parseado de LS. En fallo retorna `[]`. | `Array<Product>` |
| `getProductById()`| `id` (String) | Localiza mediante `.find(p => p.id === id)`. | `Object | Undefined` |
| `addProduct()` | `product` (Object) | Obtiene catálogo completo, inyecta propiedad aleatoria `id: 'p' + Date.now()`, `.push()` y persiste. | Objeto final mutado |
| `updateProduct()` | `updatedProduct` (Object) | Busca por Index, reemplaza índice original con la mutación y persiste. | `void` |
| `deleteProduct()` | `id` (String) | Crea nuevo array usando un `.filter()` que ignora el ID objetivo. Sobrescribe matriz global. | `void` |

### 3.3 Dominio: Patrón Carrito y Transaccionalidad
| Función | Entradas y Datos | Lógica Interna | Salida | Validaciones Críticas |
| --- | --- | --- | --- | --- |
| `addToCart()` | `productId` (Str), `quantity` (Num) default=1 | Consigue Producto. Sí el ID ya existe en Array Cart actualiza += Quantity. Si no, introduce el Objeto completo envuelto. Dispara evento custom a la Ventana: `notifyCartUpdated`. | `void` | Evita errores no procesando IDs Fantasma `if(!product) return`. |
| `updateCartItemQty()`| `productId` (Str), `quantity` (Num) | Re-calcula. Modifica variable de propiedad Quantity con asignación directa. Guarda y re-despacha el CustomEvent. | `void` | **ISO Requisito**: Si `quantity <= 0`, ejecuta autolimpiado de la entrada mediante filtrado `.filter()`. |
| `getCartTotal()` | `None` | `cart.reduce((total, item) => total + (item.product.price * item.quantity), 0)` | Number (Total Suma) | N/A |
| `placeOrder()` | `customerData` (Object) | Instancia Array Order. Genera llave Hash `ORD- + Rand(1000,9999)`. Crea objeto macro acoplando: Carrito Actual entero, Total Calculado Monetario de Instancia, Metadata de Cliente y Timestamp del dispositivo. Inserta y luego ejecuta comando Limpiador de Carrito. | `Object<Order>` u `Null` | Condición absoluta de Bloqueo Funcional Transaccional si Array de Carrito previo equivale a `0` items (`length===0`). |

### 3.4 Dominio: Controladores FrontEnd (`catalog.js / admin.js`)
- `updateCartBadge(count)`: Busca la burbuja nav `.badge`. Re-pinta la figura del contador y genera micro-animación CSS infundida por JavaScript (`transform: scale(1.2)` con reseteo asíncrono en 200 milisegundos).
- `handleSaveProduct(e)`: **VALIDACIÓN CRÍTICA ADMIN:** Inspecciona inyección de Imagen (`value`). Si el string tiene forma de Buffer Nativo Codificado `startsWith("data:image/")`, Detiene Abortando la operación derivando a `alert` impidiendo sobrecarga excesiva en Base de Datos de Texto simulado. Únicamente acepta referencias Link HTTP/S.
- `handleLogin(e)`: Proceso de Simulación Auth, encapsulando en variables inyecciones base64 (`btoa(email)` y `btoa(password)`) comprobando igualdad Hash vs Credenciales conocidas HardCodeables pseudo-seguras aisladas a texto plano ("admin@pandecasa.com" - "admin123").

---

## 4. Design System (Tokens de Diseño / Hojas CSS)

La estructura visual (`index.css`) aplica una mezcla "Utility-First" hibrida, basando la universalidad estética en variables Custom Globales en `:root`.

### 4.1 Definición de la Paleta de Colores (Hexadecimales)
- **Superficies & Fondos**
  - Fondo Global del Canvas: `#fcfcfc` `--bg-color`
  - Tarjetas puras, inputs y Navbars: `#ffffff` `--surface-color`
  - Gradiente Decorativo Exclusivo (Aparición de Hero / Sidebar Activa): Transición desde `#fff5f2` (Rosado Pálido) hacia la transparencia / canvas general.
- **Accentos (Branding Principal)**
  - Primario Puro: `#e76e55` `--primary`
  - Primario Hundido/Hover: `#d65a41` `--primary-hover`
- **Textos y Escala de Grises Monocroma**
  - Títulos/Importantes: `#333333` `--text-main`
  - Descripciones/Mutes: `#666666` `--text-muted`
  - Bordes Divisores: `#eaeaea` `--border-color`
- **Pastillas Dinámicas de Estado (Logística)**
  - *Pending (Amarillo):* Fuente `#92400e`, Fondo `#fef3c7`.
  - *Prep (Azul):* Fuente `#0369a1`, Fondo `#e0f2fe`.
  - *Ready (Verde):* Fuente `#166534`, Fondo `#dcfce7`.
  - *Delivered (Gris):* Fuente `#374151`, Fondo `#f3f4f6`.
- **Riesgos / Alertas Base:** `#ef4444` (Rojo). `rgb(231 110 85 / 15%)` y variables del 30/40% (Auras Focus en Inputs y Sombras de Botones).

### 4.2 Arquitectura Tipográfica
- **Fuente Raíz:** Google Font `'Work Sans'` (Comprobaciones en 4 pesos: 400, 500, 600, 700). Fallback: `sans-serif`. Renderizado optimizado para OSX: `-webkit-font-smoothing: antialiased`.
- **Set de Titulaciones (`h1` al `h6`):**
  - Global `font-weight: 600`, color `--text-main`, margen abajo `0.5rem`.
  - Título H1 Exclusivo: Peso ultra-denso `700`, escala masiva responsiva `2.5rem` (`3.5rem` en cabeceras Hero Desktop). Comprime tracking con `letter-spacing: -0.02em`.
  - Título H2 Exclusivo: Escala `2rem`, `letter-spacing: -0.01em`.
- **Set Cuerpo de Párrafos (`p`):** Interlineado Base Universal `line-height: 1.6`. Sub-Atenuado de contraste con `var(--text-muted)`.

### 4.3 Espaciados, Utilitarios y Rejillas CSS
- **Framework Oculto de Utilitarios:** Los elementos logran su separación mediante invocaciones CSS idénticas a herramientas modernas (`mt-1` (0.5rem), `mt-2` (1rem), `mt-3` (1.5rem)... `mt-4`, `mb-...`, `gap-2`). Centralizando utilidades de visualización (`.d-flex`, `.text-center`, `.justify-between`). Esto dictamina obligatoriamente emular estos comportamientos utilitarios al migrar UI en Componentes Modulares App.
- **Grid Lógico Fundamental de Componentes:** Invocado vía `product-grid` CSS. Utiliza Auto-Ajuste Responsivo Mínimo-Máximo para prescindir de Breakpoints `grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))`, inyectando espaciado entre cajas interno rígido de Base 2 Reales (`gap: 2rem`).

### 4.4 Sistema de Propiedades de Componentes
- **Geometría de Curvatura (Border Radius):** `--radius: 12px` (Standard Botones y Box Tarjetas). Las píldoras de Status utilizan forma de cápsula (`2rem`). El conteo Numérico (Badge) es Círculo Geométrico `50%`.
- **Volumetría Estructural (Sombras Elevation):**
  - `--shadow-sm: 0 2px 8px rgb(0 0 0 / 5%)` (Falso Z-index = 1. Estado Reposo de Interfaz).
  - `--shadow-md: 0 8px 24px rgb(0 0 0 / 8%)` (Falso Z-index = 2. Estado Elevado de Tarjetas Modal).
- **Interacciones Relevantes:**
  - *Botones Acción:* Generan Efecto 3D por Ilusión Optica CSS: Desplazamiento Y Elevativo (Hover transiciona a `-2px` y activa sombras teñidas con Aura de `--primary`). El disparo final presionado clava un recálculo abrupto de rebote a Y en `0`.
  - *Inputs Entrada:* Estado En Foco (`:focus`) erradica el delineado nativo y esparce halo translúcido rojo perimetral `box-shadow: 0 0 0 3px rgb(231 110 85 / 15%)`.
  - *Animación de Flujo Carga (`.animate-fade-in`):* Todos los contenedores base resuelven carga por Opacidad 0 -> 1 subiendo por el vector eje Y 10 Píxeles en 400ms.

---

## 5. Modelo de Datos y Estado Estructural (Schema Arquitecture)
JSON Arrays Modulados

**1. `Product` (Item Estático de Catálogo)**
```json
{
  "id": "p6",               // Identificador Alfanumérico del elemento.
  "name": "Pan de Centeno", // Título. String simple.
  "description": "...",     // Base Narrativa. String.
  "price": 75.00,           // Flotante 2 pos / Entero Financiero. Representado para UI sin divisa interna.
  "image": "http..."        // Enlace Web Remoto. Sin inyecciones de base 64 admitidas.
}
```

**2. `CartItem` (Mutación Acoplada al Carrito)**
```json
{
  "product": { /* Entidad Product íntegra Inmutable replicada (Ver Arriba ) */ },
  "quantity": 1   // Integer Entero. Valor Activo Escalable por Evento Plus/Minus en Controlador Cart HTML.
}
```

**3. `Order` (Entidad Completa Bloqueada y Fija Histórica Generada)**
```json
{
  "id": "ORD-1234",          // Llave Primaria Operacional generada pseudoaleatoriamente en Submit checkout.
  "items": [ /* Arreglo Mutado idéntico a una foto del CartItem Completo Pre-Checkout */ ],
  "total": 85.00,            // Costo Finito Acumulado congelado.
  "customer": {
      "name": "String", "phone": "String", "email": "String", 
      "address": "String", "zip": "Str/Num", "city": "String", 
      "notes": "String", "paymentMethod": "card_delivery|cash_delivery|transfer"
  },
  "status": "pending",       // Estado Operativo Abierto de Iteración Escalar
  "createdAt": 171120489000  // Unix Epoch milisegundos inalcanzable.
}
```

---

## 6. Diagrama General de Flujos de Usuario Reales (Step-by-Step)

### Eslabón 1: Cliente descubre el sistema, interactúa y convierte.
1. Renderizado Dinámico y Securización de Datos: En `<div id="productGrid">`, el JS extrae del Archivo `Store` el listado e inyecta iterándolo y escapando vulnerabilidades XSS por cada campo de su Metadata.
2. In-App Sincronía: Al Accionar `addToCart()`, se despacha en la Raíz de Aplicación Global (Publish/Subscribe Architect) la Señal Virtual `cartUpdated`. El NavBar en Head escucha el susurro asincrónico invocando a `updateCartBadge()` pintando y animando el conteo superior lateral evitando re-cargas.
3. El cliente navega físicamente hacia `cart.html`. Reconstrucciones interactivas permiten alterar `quantity`. Si resta el sumatorio `< 0`, el Item desaparece del Arreglo Lógico persistiendo la limpieza en `localStorage`.
4. El botón "Proceder al Pago" direcciona pasivamente su instancia local redirigiéndole para llenar un Bloque Nativo de HTML5 Validator (`checkout.html`). Al Disparar POST Simulativo, El sistema clona internamente todos los datos y limpia el Buffer temporal generando un Objeto Pesado `Orders`, Redirigiendo con parámetro explícito `confirmation.html?orderId=ORD-XXXX`.

### Eslabón 2: Tracking Sostenido y Operatividad Operacional BackEnd.
1. **Cliente Post-Compra (`order-status.html`):** Establece una sonda iterativa `setInterval` constante temporal cada 5 Segundos de ejecución para el recálculo y repintado HTML. 
2. Asignación de Progreso: Identifica el Estado Variable Nativo (`"pending", "prep", etc`) extraído de LocalStorage. Realiza una asignación comparativa Matemática para dibujar una representación porcentual en Línea vector mediante Regla De 3 Divisora `(currentStatusIndex / 3) * 100%`, moviendo visualmente una transición continua la línea `background: var(--primary)` en los círculos generados.
3. **Rol Exclusivo Operario (`Dashboard -> orders.html`):** Entra tras Check Auth y verifica una tabla generada reactivamente del Objeto Macro de Sistema. 
4. Avance Finito Manual Logístico: Los botones generan `updateStatus()` escalando mediante Mapa Fijo Declarado Variables Hacia Arriba -> `'pending'` cambia a `'prep'`, `'prep'` deriva a `'ready'`, `'ready'` empuja a `'delivered'` desactivando su seguimiento operativo en reportes futuros y de cocina global.
5. Reporte Consolidado Diario Inteligente: (`dashboard.html -> productionTable`). JS crea en memoria diccionarios dinámicos filtrando Pedidos en estado Finalizado. Si la orden está Activa, recorre todos Items de Pan; Acumula en un Objeto temporal Claves Idénticas y emite el requerimiento sumado único (Ej: Ignora quién ordenó, fusiona qué requiere cocinar la panadería hoy "Pan Centeno: Total a Hacer 15").

### Eslabón 3: Gestión del Catálogo y ABM de Productos (Admin)
1. El Administrador navega hacia el controlador visual de catálogo (`admin/product-form.html`).
2. **Creación/Edición Unificada:** Un único formulario sirve ambos propósitos. Al rellenar "Nombre", "Precio", "URL de Imagen" y "Descripción", se desencadena el Evento Submit (`handleSaveProduct`).
3. **Validación Exhaustiva:** El controlador inspecciona estrictamente la URL de la imagen. Si el string empieza con `data:image/` descarta la promesa, frena la operación y muestra aviso (`alert`) bloqueando el consumo desproporcionado de caracteres base64 en memoria de `localStorage`.
4. Si se supera la validación y el formulario no tiene Identificador oculto (`productId` vacío), inyecta en array Maestro con un ID originado por Tiempo-Algoritmo. Si tiene ID, pisa/reemplaza el registro actualizando el Item en todas las ramas de catálogo.
5. El sistema limpia obligatoriamente el formulario (`resetForm()`) y re-pinta reactivamente la UI secundaria sin refrescar página completa.
6. **Borrado (Delete):** Al accionar "Borrar" sobre un cuadro, surge Pop-Up confirmando (`confirm()`). Sí es verdadero, inyecta supresión en cascada (Array sin elemento eliminado).
