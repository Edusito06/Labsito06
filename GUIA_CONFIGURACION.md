# Guía de configuración: Firebase y Facebook (Windows)

Datos que usarás en toda la guía:

- **Package name de la app:** `com.lab6.iot`
- **Contraseña del keystore de debug:** `android`
- **Terminal:** PowerShell (clic derecho en el botón Inicio → Terminal / Windows PowerShell).
  No necesitas ser administrador.

Haz las cosas en este orden. Si cambias el orden, tendrás que volver a descargar `google-services.json`.

---

## 0. Traer el proyecto y abrirlo

1. Instala **Git for Windows** (https://git-scm.com/download/win) si no lo tienes.
2. En PowerShell:

```powershell
git clone https://github.com/Edusito06/Labsito06.git
```

3. Abre **Android Studio → File → Open** y elige la carpeta clonada (la que contiene `settings.gradle`).
   Espera a que termine el *Gradle Sync*. La primera vez descarga cosas y tarda unos minutos.
4. Si Android Studio pide instalar el **Android SDK Platform 34**, acepta.

---

## 1. Obtener tu SHA-1 y SHA-256 (huellas de tu computadora)

Google necesita estas "huellas" para saber que la app que pide iniciar sesión es la tuya.
Se generan a partir del keystore de debug que crea Android Studio, en
`C:\Users\TU_USUARIO\.android\debug.keystore`.

**Opción A: con el comando `keytool`.** Ejecuta en PowerShell:

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Copia las líneas **SHA1** y **SHA256** del resultado.

- Si dice que no encuentra `keytool.exe`, Android Studio está en otra ruta. Búscalo con
  `Get-ChildItem "C:\Program Files" -Recurse -Filter keytool.exe -ErrorAction SilentlyContinue`.
- Si dice que el keystore no existe, corre la app una vez desde Android Studio y vuelve a intentar.
  El keystore se crea al compilar en modo debug.

**Opción B: desde Android Studio.** Abre el panel **Gradle** (barra derecha) → `Lab6IoT` → `app` →
`Tasks` → `android` → doble clic en **signingReport**. Las huellas salen en la pestaña *Run*.
En el proyecto no hay `gradlew.bat`, así que no uses la terminal para esto.

**Si el laboratorio es en otra PC, sus huellas serán distintas.** Repite este paso allí y agrégalas
en Firebase (sección 2, paso 4) y en Meta (sección 3, paso 6).

---

## 2. Firebase

Firebase es un servicio de Google. Aquí lo usas para dos cosas: **Authentication** (usuarios y login) y
**Realtime Database** (guardar los datos).

1. Entra a https://console.firebase.google.com con tu cuenta de Google.
2. **Crear un proyecto** (Add project). Ponle un nombre (por ejemplo `lab6-iot`). Google Analytics puedes desactivarlo.
3. En la pantalla principal del proyecto pulsa el ícono de **Android**. Se llama "Agregar app".
   - **Nombre del paquete:** `com.lab6.iot`. Debe ser exactamente ese.
   - Apodo: lo que quieras.
   - **Certificado SHA-1:** pega el SHA1 del paso 1.
   - Pulsa **Registrar app**. Omite los pasos siguientes del asistente (ya están hechos en el proyecto).
4. **Configuración del proyecto** (engranaje) → pestaña **General** → tu app Android →
   **Agregar huella digital** y pega también el **SHA-256**.

### 2.1 Authentication
1. Menú izquierdo: **Compilación → Authentication → Comenzar**.
2. Pestaña **Sign-in method** (Método de acceso). Habilita:
   - **Correo electrónico/contraseña** → Activar → Guardar.
   - **Google** → Activar → elige un correo de asistencia del proyecto → Guardar.
   - **Facebook** → déjalo para la sección 3. Necesita el App ID y el App Secret de Meta.

### 2.2 Realtime Database
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

### 2.3 Descargar google-services.json (siempre al final)
1. **Configuración del proyecto → General → tu app Android → `google-services.json`**.
2. Descárgalo y **reemplaza** el archivo `app\google-services.json` del proyecto
   (la carpeta `app` que está dentro de la carpeta del proyecto, junto a `build.gradle`).
3. Ábrelo con el Bloc de notas y verifica que tenga `"oauth_client"` con un `client_type: 3`.
   Ese es el que usa Google Sign-In. Si no aparece, revisa que agregaste el SHA-1 y habilitaste Google,
   y vuelve a descargarlo.
4. En Android Studio pulsa **Sync Project with Gradle Files** (ícono del elefante con flecha).

---

## 3. Facebook (Meta for Developers)

Para que el botón "Continuar con Facebook" funcione, Facebook debe conocer tu app.

1. Entra a https://developers.facebook.com con tu cuenta de Facebook.
   La primera vez te pide registrarte como desarrollador (aceptar términos y verificar teléfono o correo).
2. **Mis apps → Crear app**.
   - Nombre (por ejemplo `Lab6 IoT`) y correo de contacto.
   - Caso de uso: **Autenticar y solicitar datos de usuarios con el inicio de sesión con Facebook**
     (Authenticate and request data from users with Facebook Login).
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
   - **Hashes de clave** (Key Hashes): es la huella SHA-1 del paso 1, convertida a Base64.
     Windows no trae `openssl`, así que se hace con PowerShell. Reemplaza el texto entre comillas por tu SHA1 tal cual
     salió (con los dos puntos):

```powershell
$sha1 = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD"
[Convert]::ToBase64String(($sha1 -split ':' | ForEach-Object { [Convert]::ToByte($_, 16) }))
```

   El resultado es un texto de 28 caracteres que termina en `=`. Pégalo en **Hashes de clave** y guarda.
   Si cambias de PC, repite esto con el SHA1 de esa PC y agrega ese hash también.
   Alternativa: si más adelante Facebook responde "Invalid key hash", el mensaje de error muestra el hash que recibió.
   Puedes pegarlo tal cual en Meta.
7. **Token de cliente** (Client Token): **Configuración → Avanzada → Token de cliente**. Cópialo.
8. Abre `app\src\main\res\values\strings.xml` y reemplaza:

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

## 4. Correr la app

1. En Android Studio, abre **Device Manager** y crea un emulador **con Google Play**
   (en la lista elige un dispositivo que tenga el ícono de Play Store, y una imagen del sistema API 34).
   Sin Google Play, el login de Google no funciona.
2. Si el emulador no arranca y menciona virtualización o HAXM, activa **Windows Hypervisor Platform** y
   **Virtual Machine Platform** (Panel de control → Activar o desactivar las características de Windows)
   y revisa que la virtualización esté activada en la BIOS. Si no puedes, usa un teléfono real con depuración USB.
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
| Facebook: "Invalid key hash" | Falta el hash de la sección 3, paso 6, o se calculó con otro SHA-1. El mensaje de error muestra el hash que Facebook recibió. |
| Facebook: "App not active" o no deja entrar | La cuenta no tiene rol en la app de Meta (modo desarrollo). |
| Firebase: `Permission denied` | No iniciaste sesión, o las reglas no se publicaron. |
| El Gradle Sync no encuentra `MPAndroidChart` | Revisa que `settings.gradle` tenga el repositorio `jitpack.io`. Ya está incluido. |
| Al abrir la app dice "REEMPLAZAR" o falla el login | Sigues con el `google-services.json` placeholder. |
| `keytool` no se reconoce como comando | Usa la ruta completa de la sección 1 (con `&` y las comillas). |
