# Guía de configuración: Firebase y Facebook

Datos que usarás en toda la guía:

- **Package name de la app:** `com.lab6.iot`
- **Contraseña del keystore de debug:** `android`

Haz las cosas en este orden. Si cambias el orden, tendrás que volver a descargar `google-services.json`.

---

## 0. Obtener tu SHA-1 y SHA-256 (huellas de tu computadora)

Google necesita estas "huellas" para saber que la app que pide iniciar sesión es la tuya.
Se generan a partir del keystore de debug que crea Android Studio.

Abre la Terminal y ejecuta:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copia las líneas **SHA1** y **SHA256**. Si da "keystore no existe", abre y corre la app una vez en
Android Studio para que se cree. Si dice "Unable to locate a Java Runtime", usa el JDK de
Android Studio:

```bash
"/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

También puedes usar Android Studio: panel **Gradle** (derecha) → `Lab6IoT` → `app` → `Tasks` → `android` → `signingReport`.

**Si el laboratorio es en otra PC, esas huellas serán distintas.** Hay que agregarlas también en Firebase (sección 1, paso 3).

---

## 1. Firebase

Firebase es un servicio de Google. Aquí lo usas para dos cosas: **Authentication** (usuarios y login) y **Realtime Database** (guardar los datos).

1. Entra a https://console.firebase.google.com con tu cuenta de Google.
2. **Crear un proyecto** (Add project). Ponle un nombre (por ejemplo `lab6-iot`). Google Analytics puedes desactivarlo.
3. En la pantalla principal del proyecto pulsa el ícono de **Android**. Se llama "Agregar app".
   - **Nombre del paquete:** `com.lab6.iot`. Debe ser exactamente ese.
   - Apodo: lo que quieras.
   - **Certificado SHA-1:** pega el SHA1 del paso 0.
   - Pulsa **Registrar app**. Omite los pasos siguientes del asistente (ya están hechos en el proyecto).
4. En **Configuración del proyecto** (engranaje) → pestaña **General** → tu app Android → pulsa **Agregar huella digital** y pega también el **SHA-256**.

### 1.1 Authentication
1. Menú izquierdo: **Compilación → Authentication → Comenzar**.
2. Pestaña **Sign-in method** (Método de acceso). Habilita:
   - **Correo electrónico/contraseña** → Activar → Guardar.
   - **Google** → Activar → elige un correo de asistencia del proyecto → Guardar.
   - **Facebook** → déjalo para la sección 2. Necesita el App ID y el App Secret de Meta.

### 1.2 Realtime Database
1. Menú: **Compilación → Realtime Database → Crear base de datos**.
2. Elige ubicación (la de EE. UU. por defecto está bien) y modo **bloqueado**.
3. Pestaña **Reglas** → reemplaza todo por esto y pulsa **Publicar**:

```json
{
  "rules": {
    "registros": {
      "$uid": {
        ".read": "auth.uid === $uid",
        ".write": "auth.uid === $uid"
      }
    }
  }
}
```

Esto hace que cada usuario solo lea y escriba sus propios datos. Es la parte de "seguridad de la información" del RA1.

### 1.3 Descargar google-services.json (siempre al final)
1. **Configuración del proyecto → General → tu app Android → `google-services.json`**.
2. Descárgalo y **reemplaza** el archivo `Lab6App/app/google-services.json`.
3. Ábrelo y verifica que dentro tenga `"oauth_client"` con un `client_type: 3`. Ese es el que usa Google Sign-In.
   Si no aparece, revisa que agregaste el SHA-1 y habilitaste Google, y vuelve a descargarlo.

---

## 2. Facebook (Meta for Developers)

Para que el botón "Continuar con Facebook" funcione, Facebook debe conocer tu app.

1. Entra a https://developers.facebook.com con tu cuenta de Facebook.
   La primera vez te pide registrarte como desarrollador (aceptar términos y verificar teléfono o correo).
2. **Mis apps → Crear app**.
   - Nombre (por ejemplo `Lab6 IoT`) y correo de contacto.
   - Caso de uso: **Autenticar y solicitar datos de usuarios con el inicio de sesión con Facebook** (Authenticate and request data from users with Facebook Login).
   - Tipo "Consumidor" si lo pregunta. Crea la app.
   - Meta cambia los menús seguido. Si no ves una opción, busca "Facebook Login" en el panel.
3. **Configuración de la app → Básica** (App settings → Basic). De ahí copia:
   - **Identificador de la app** (App ID).
   - **Clave secreta de la app** (App Secret): pulsa "Mostrar".
4. Vuelve a **Firebase → Authentication → Sign-in method → Facebook** → Activar:
   - Pega el App ID y el App Secret.
   - Firebase te muestra una **URI de redireccionamiento de OAuth**, algo como
     `https://lab6-iot.firebaseapp.com/__/auth/handler`. Cópiala y pulsa **Guardar**.
5. De vuelta en Meta: en el panel entra a **Inicio de sesión con Facebook → Configuración**
   (Facebook Login → Settings) y pega esa URI en **URI de redireccionamiento de OAuth válidos**. Guarda.
6. En **Configuración → Básica**, baja y pulsa **Agregar plataforma → Android**:
   - **Nombre del paquete:** `com.lab6.iot`
   - **Nombre de clase de actividad predeterminada:** `com.lab6.iot.LoginActivity`
   - **Hashes de clave** (Key Hashes): hay que generarlo con este comando (te pedirá la contraseña `android`):

```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
```

   Pega el resultado y guarda. Si cambias de PC, repite el comando y agrega ese hash también.
7. **Token de cliente** (Client Token): **Configuración → Avanzada → Token de cliente**. Cópialo.
8. Abre `app/src/main/res/values/strings.xml` y reemplaza:

```xml
<string name="facebook_app_id">123456789012345</string>
<string name="facebook_client_token">abcdef1234567890</string>
<string name="fb_login_protocol_scheme">fb123456789012345</string>
```

   La tercera es `fb` seguido del App ID, sin espacio.

### Modo desarrollo
La app de Meta nace en **modo Desarrollo**. En ese modo solo pueden iniciar sesión las cuentas con
rol en la app (tu cuenta de Facebook, la que la creó, ya es Administrador). No necesitas publicarla ni pasar
revisión. Para probar con otra cuenta: **Roles de la app → Roles → Agregar personas → Probadores**.

---

## 3. Correr la app

1. Android Studio → **File → Open** → carpeta `Lab6App` → espera el *Gradle Sync*.
2. Para correr necesitas un emulador **con Google Play** (en Device Manager elige una imagen que tenga el ícono de Play Store) o un teléfono real con depuración USB. Google Sign-In no funciona en imágenes sin Google Play.
3. Pulsa **Run** (el triángulo verde).

### Lista de pruebas
- [ ] Registrarse con correo y contraseña y ver que entra a la pantalla de inicio.
- [ ] Salir e iniciar con ese correo.
- [ ] "¿Olvidaste tu contraseña?" y que llegue el correo (revisa spam).
- [ ] Continuar con Google.
- [ ] Continuar con Facebook.
- [ ] Agregar, editar y borrar un registro. Verifica en la consola de Firebase que aparece en `registros/<uid>`.
- [ ] Agregar varios registros de distintos tipos, abrir Gráficos y cambiar el filtro.

### Errores comunes
| Síntoma | Causa |
|---|---|
| Google: `Error con Google: 10` o `12500` | Falta el SHA-1 en Firebase, o el `google-services.json` es viejo o no tiene `oauth_client`. Agrega el SHA-1, descarga otra vez el JSON y compila de nuevo. |
| Facebook: "Invalid key hash" | Falta el hash de la sección 2, paso 6. El mensaje de error muestra el hash que Facebook recibió, y puedes pegarlo tal cual en Meta. |
| Facebook: "App not active" o no deja entrar | La cuenta no tiene rol en la app de Meta (modo desarrollo). |
| Firebase: `Permission denied` | No iniciaste sesión, o las reglas no se publicaron. |
| El Gradle Sync no encuentra `MPAndroidChart` | Revisa que `settings.gradle` tenga el repositorio `jitpack.io`. Ya está incluido. |
| Al abrir la app dice "REEMPLAZAR" o falla el login | Sigues con el `google-services.json` placeholder. |
