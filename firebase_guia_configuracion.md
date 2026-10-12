# Guía de Configuración y Migración a Google Firebase - Pan de Casa (SENA)

Esta guía documenta la migración de la base de datos de **Supabase** a **Google Firebase**, las validaciones realizadas y los pasos necesarios en la consola de Firebase.

---

## 1. Validación del Diagnóstico

El análisis del proyecto confirmó que la arquitectura original y los lineamientos del SENA (Fase 7 de Desarrollo Móvil) estaban destinados a **Google Firebase**:

1. **`FirebaseModule.kt`**: Ya existía en `app/src/main/java/com/pan_de_casa/di/` preparado para Firebase.
2. **`libs.versions.toml`**: Ya incluía la versión del plugin `googleServices = "4.4.2"` y la declaración del plugin de Google.
3. **`OrderStatusScreen.kt`**: En la línea 60 tenía el comentario explícito: `// Esto vendría del Flow de Firebase`.
4. **`domain/model/User.kt`**: Utiliza el identificador `uid` (estándar nativo de `FirebaseUser.getUid()`).
5. **Estándar SENA**: En los programas de formación de desarrollo de software y aplicaciones móviles, el backend estándar evaluado para Android con Kotlin es **Google Firebase** (Authentication + Cloud Firestore).

---

## 2. Cambios Ya Realizados en el Código

El proyecto ha sido completamente refactorizado a Firebase:

| Componente | Antes (Supabase) | Ahora (Google Firebase) |
| :--- | :--- | :--- |
| **Gradle Plugins** | Sin plugin de servicios | `com.google.gms.google-services` activo |
| **Dependencias** | `supabase-bom`, `postgrest-kt`, `ktor` | `firebase-bom:33.7.0`, `firebase-auth`, `firebase-firestore`, `firebase-storage`, `kotlinx-coroutines-play-services` |
| **Módulo DI** | `SupabaseModule` | `FirebaseModule` inyectando `FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage` |
| **Autenticación** | `SupabaseAuth` | `FirebaseAuth` + perfil en Firestore (`users/{uid}`) |
| **Catálogo** | `postgrest.from("products")` | `firestore.collection("products")` con caché Room |
| **Pedidos** | `orders` + `order_items` relacional | `firestore.collection("orders")` con items NoSQL embebidos y Flow en tiempo real |
| **Seguimiento UI** | Estado simulado estático | `OrderStatusScreen` conectado reactivamente al Flow de Firestore |

---

## 3. Pasos a Seguir en la Consola de Firebase

Para que la base de datos se conecte a tu propia cuenta de Google, sigue estos pasos:

### Paso 1: Crear el Proyecto en Firebase
1. Ve a [Firebase Console](https://console.firebase.google.com/) e inicia sesión con tu cuenta de Google.
2. Haz clic en **"Agregar proyecto"** o **"Crear proyecto"**.
3. Nómbralo **Pan de Casa** (o el nombre que prefieras) y haz clic en Continuar.
4. Desactiva Google Analytics si no lo requieres para simplificar el proceso, o déjalo activo.

### Paso 2: Registrar la Aplicación Android
1. En la pantalla principal del proyecto, haz clic en el ícono de **Android**.
2. En **Nombre del paquete de Android**, ingresa exactamente:
   ```text
   com.pan_de_casa
   ```
3. (Opcional) En apodo escribe: `Pan de Casa App`.
4. Haz clic en **Registrar app**.

### Paso 3: Descargar y Colocar `google-services.json`
1. Descarga el archivo generado **`google-services.json`**.
2. Cópialo y pégalo en la carpeta del proyecto reemplazando el archivo temporal que creamos:
   ```text
   PAN_CASA/app/google-services.json
   ```

### Paso 4: Habilitar los Servicios en Firebase Console

#### A. Authentication (Autenticación)
1. En el menú lateral izquierdo, ve a **Compilación** -> **Authentication**.
2. Haz clic en **Comenzar**.
3. En la pestaña **Método de acceso** (Sign-in method), selecciona **Correo electrónico/Contraseña**.
4. Habilita la primera casilla (Habilitar) y haz clic en **Guardar**.

#### B. Cloud Firestore (Base de Datos NoSQL)
1. En el menú lateral, ve a **Compilación** -> **Firestore Database**.
2. Haz clic en **Crear base de datos**.
3. Selecciona la ubicación geográfica (ej. `nam5 (us-central)` o la más cercana).
4. En reglas de seguridad, selecciona **"Comenzar en modo de prueba"** (permite lectura/escritura inmediata durante el desarrollo).
5. Haz clic en **Crear**.

#### C. Firebase Storage (Para fotos de panes - Opcional)
1. En el menú lateral, ve a **Compilación** -> **Storage**.
2. Haz clic en **Comenzar** y elige la opción en **modo de prueba**.

---

## 4. Estructura de Colecciones en Firestore

No necesitas crear scripts SQL complejos. En Firestore, las colecciones se crean automáticamente cuando se inserta el primer documento.

### Colección `users`:
Guarda los datos extendidos del usuario y su rol:
```json
{
  "uid": "ID_DE_FIREBASE_AUTH",
  "name": "Nombre Usuario",
  "email": "usuario@pandecasa.com",
  "role": "CLIENT",
  "address": "Calle 10 # 20 - 30",
  "phone": "3001234567",
  "createdAt": 171120489000
}
```

> [!TIP]
> **Crear usuario Administrador:**
> Puedes registrarte normalmente desde la pantalla de Registro de la App móvil. Luego, vas a la consola de Firestore, abres el documento de tu usuario en la colección `users` y cambias el campo `"role"` de `"CLIENT"` a `"ADMIN"`. Con eso tendrás acceso total al panel administrativo.

### Colección `products`:
Catálogo de productos:
```json
{
  "name": "Pan Campesino",
  "description": "Pan tradicional horneado en leña con corteza crujiente.",
  "price": 4500.0,
  "imageUrl": "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600",
  "category": "Tradicional",
  "createdAt": 171120489000
}
```

### Colección `orders`:
Pedidos realizados con seguimiento en tiempo real:
```json
{
  "id": "ORD-123456",
  "customerId": "uid_del_cliente",
  "customerName": "Juan Pérez",
  "customerAddress": "Calle 15 # 4 - 20",
  "customerPhone": "3123456789",
  "total": 13500.0,
  "status": "RECIBIDO",
  "createdAt": 171120489000,
  "items": [
    {
      "productId": "id_del_pan",
      "productName": "Pan Campesino",
      "quantity": 3,
      "priceAtTime": 4500.0
    }
  ]
}
```
