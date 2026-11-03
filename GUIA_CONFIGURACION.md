# Guía paso a paso para el Laboratorio 6 (Windows)

Sigue los pasos **en orden** y marca cada casilla. Si te saltas el orden, tendrás que repetir pasos.
Lo más lento es Facebook, así que **si puedes, haz todo esto antes del laboratorio**.

- **Package name de la app (siempre el mismo):** `com.lab6.iot`
- **Contraseña del keystore de debug (siempre la misma):** `android`
- **Terminal:** PowerShell. Clic derecho en el botón Inicio → *Terminal* o *Windows PowerShell*. No necesitas ser administrador.

---

## Tabla de datos (anótalos aquí a medida que los obtengas)

| Dato | Dónde sale | Tu valor |
|---|---|---|
| SHA-1 | Paso 2 | |
| SHA-256 | Paso 2 | |
| Hash de clave de Facebook | Paso 5 (ejecuta el script) | |
| App ID de Facebook | Meta → Configuración → Básica | |
| App Secret de Facebook | Meta → Configuración → Básica → Mostrar | |
| Client Token de Facebook | Meta → Configuración → Avanzada | |
| URI de redirección | Firebase → Authentication → Facebook | |

---

## Paso 1. Preparar Windows y abrir el proyecto

- [ ] Tener instalado **Git for Windows** (https://git-scm.com/download/win). Comprueba con `git --version`.
- [ ] Tener **Android Studio** instalado. Ruta habitual: `C:\Program Files\Android\Android Studio`.
- [ ] Clonar el repo. En PowerShell, en la carpeta donde quieras el proyecto (por ejemplo `cd C:\Users\TU_USUARIO\Documents`):

```powershell
git clone https://github.com/Edusito06/Labsito06.git
```

- [ ] En Android Studio: **File → Open** → elige la carpeta `Labsito06`, la que contiene `settings.gradle`
  (no la carpeta `app`) → **OK** → **Trust Project**.
- [ ] Espera el **Gradle Sync**. La primera vez descarga mucho y tarda 3 a 10 minutos. Mira la barra de abajo a la derecha.
- [ ] Instala el SDK 34 si Android Studio lo pide: **Tools → SDK Manager → SDK Platforms** → marca
  **Android 14.0 ("UpsideDownCake") API 34** → **Apply** → **OK**. En el laboratorio anterior usaste API 36, así que 34 es probable que no lo tengas instalado.
- [ ] Si Android Studio ofrece **actualizar el plugin de Gradle (AGP Upgrade Assistant)**, pulsa **Remind me later** o ciérralo. No lo actualices.
  Este proyecto usa AGP 8.5.2 y Gradle 8.7 a propósito.

**Si el Sync falla por el JDK** (mensaje sobre Java o "Gradle JDK"):
**File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** →
elige **jbr-21** (o *Embedded JDK*) → **OK** → **File → Sync Project with Gradle Files**.

**Si falla por `jitpack`:** comprueba que tienes internet. Es de donde se baja la librería de gráficos.
El Sync mostrará errores en `google-services.json` hasta que hagas el Paso 4. Eso es normal.

---

## Paso 2. Obtener tus huellas SHA-1 y SHA-256

Google y Facebook necesitan estas huellas para confiar en tu app. Salen del archivo
`C:\Users\TU_USUARIO\.android\debug.keystore`, que crea Android Studio al compilar en modo debug.

**Si el archivo no existe**, corre la app una vez con el triángulo verde (aunque falle) y vuelve a esto.

### Opción A. Con `gradlew.bat` (recomendada)

En PowerShell, dentro de la carpeta del proyecto:

```powershell
cd C:\Users\TU_USUARIO\Documents\Labsito06
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat signingReport
```

Busca el bloque `Variant: debug` y copia las líneas **SHA1** y **SHA-256**.
Si te sale un error por la ruta de Java, ver "Errores comunes" al final.

### Opción B. Con `keytool`

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Copia las líneas **SHA1** y **SHA256** del resultado.

### Opción C. Desde Android Studio

Panel **Gradle** (barra derecha) → `Labsito06` → `app` → `Tasks` → `android` → doble clic en **signingReport**.
Las huellas salen en la pestaña *Run* de abajo.

- [ ] Anotaste SHA-1 y SHA-256 en la tabla de arriba.

---

## Paso 3. Crear el proyecto en Firebase

- [ ] Entra a https://console.firebase.google.com con tu cuenta de Google.
- [ ] **Crear un proyecto** (Add project) → nombre `lab6-iot` → **Continuar**.
  En Google Analytics desactiva el interruptor → **Crear proyecto** → espera → **Continuar**.
- [ ] En la pantalla principal pulsa el ícono de **Android** (el robot verde). Si no lo ves, es el botón
  **Agregar app** y luego Android.
  - **Nombre del paquete de Android:** `com.lab6.iot`. Debe ser exactamente ese.
  - **Sobrenombre:** `Lab6`.
  - **Certificado de depuración SHA-1:** pega tu SHA-1.
  - Pulsa **Registrar app**. En el siguiente paso **no descargues** todavía el archivo. Pulsa **Siguiente**
  en los pasos que quedan, hasta **Continuar a la consola**.
- [ ] Engranaje (arriba a la izquierda) → **Configuración del proyecto** → pestaña **General** → baja a *Tus apps*
  → tu app Android → **Agregar huella digital** → pega el **SHA-256** → **Guardar**.

---

## Paso 4. Activar Authentication, Database y descargar el JSON

### 4.1 Correo/contraseña y Google
- [ ] Menú izquierdo → **Compilación (Build) → Authentication → Comenzar**.
- [ ] Pestaña **Método de acceso (Sign-in method)**.
- [ ] **Correo electrónico/contraseña**: Activar el primer interruptor (el de abajo, "Vínculo de correo", déjalo apagado) → **Guardar**.
- [ ] **Google**: Activar → **Nombre público del proyecto**: lo que sea → **Correo de asistencia**: elige el tuyo → **Guardar**.
- [ ] **Facebook**: déjalo para el Paso 5.

### 4.2 Realtime Database
- [ ] Menú izquierdo → **Compilación → Realtime Database → Crear base de datos**.
- [ ] Ubicación: la que sale por defecto (Estados Unidos) → **Siguiente**.
- [ ] Modo: **Bloqueado (Locked mode)** → **Habilitar**.
- [ ] Pestaña **Reglas (Rules)** → borra todo y pega esto → **Publicar**:

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

### 4.3 Descargar `google-services.json` (siempre al final)
- [ ] Engranaje → **Configuración del proyecto → General → Tus apps → tu app Android → botón `google-services.json`**.
- [ ] Ábrelo con el Bloc de notas y busca `"oauth_client"`: debe tener una entrada con `"client_type": 3`.
  Y busca `"firebase_url"`: debe aparecer. Si falta alguno, vuelve a revisar el SHA-1 y que Google esté
  activado en Authentication y vuelve a descargar.
- [ ] Copia ese archivo y reemplaza este: `Labsito06\app\google-services.json`
  (dentro de la carpeta `app`, al lado de `build.gradle`).
- [ ] En Android Studio: **File → Sync Project with Gradle Files** (o el ícono del elefante).

---

## Paso 5. Facebook (Meta for Developers)

### 5.1 Crear la app
- [ ] Entra a https://developers.facebook.com e inicia sesión con tu cuenta de Facebook.
  La primera vez te pide **Comenzar / Get Started**, aceptar términos y verificar teléfono o correo.
- [ ] **Mis apps (My Apps) → Crear app (Create App)**.
- [ ] Nombre: `Lab6 IoT`. Correo de contacto: el tuyo. **Siguiente**.
- [ ] Caso de uso: **Autenticar y solicitar datos de los usuarios con el inicio de sesión con Facebook**
  (Authenticate and request data from users with Facebook Login) → **Siguiente**.
- [ ] Si te pregunta por un portfolio comercial, elige **No quiero conectar un portfolio** (o *Omitir*).
- [ ] Revisa y pulsa **Ir al panel / Crear app**. Puede pedirte tu contraseña de Facebook.
- Los menús de Meta cambian seguido. Si no encuentras una opción, usa el buscador del panel
  o el menú izquierdo **Configuración de la app → Básica**.

### 5.2 Copiar App ID y App Secret
- [ ] Menú izquierdo → **Configuración de la app (App settings) → Básica (Basic)**.
- [ ] Copia el **Identificador de la app (App ID)** y anótalo.
- [ ] En **Clave secreta de la app (App secret)** pulsa **Mostrar**, copia y anota. **No la subas a GitHub.**

### 5.3 Activar Facebook en Firebase
- [ ] Firebase → **Authentication → Método de acceso → Facebook → Activar**.
- [ ] Pega **App ID** y **App Secret**.
- [ ] En el mismo cuadro aparece una **URI de redireccionamiento de OAuth** (termina en `/__/auth/handler`).
  **Cópiala y anótala.** Pulsa **Guardar**.

### 5.4 Pegar la URI en Meta
- [ ] En Meta, menú izquierdo → **Casos de uso (Use cases)** → **Autenticación…** → **Personalizar** →
  **Configuración** del Inicio de sesión con Facebook.
  (Si ves directamente **Inicio de sesión con Facebook → Configuración**, entra ahí.)
- [ ] En **URI de redireccionamiento de OAuth válidos** pega la URI y pulsa **Guardar cambios**.

### 5.5 Agregar la plataforma Android
- [ ] **Configuración de la app → Básica**, baja al final → **Agregar plataforma (Add platform) → Android**.
- [ ] **Nombre del paquete de Google Play:** `com.lab6.iot`
- [ ] **Nombre de clase:** `com.lab6.iot.LoginActivity`
- [ ] **Hashes de clave (Key Hashes):** se calcula en el paso siguiente.

### 5.6 Calcular el hash de clave
Windows no trae `openssl`, así que el hash (tu SHA-1 convertido a Base64) se calcula con PowerShell.
Cambia el texto entre comillas por **tu SHA-1 tal cual salió, con los dos puntos**:

```powershell
$sha1 = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD"
[Convert]::ToBase64String(($sha1 -split ':' | ForEach-Object { [Convert]::ToByte($_, 16) }))
```

- [ ] El resultado es un texto de 28 caracteres que termina en `=`. Cópialo, anótalo y pégalo en
  **Hashes de clave** → **Guardar cambios**.
- [ ] Si más adelante Facebook responde *"Invalid key hash"*, el mensaje de error muestra el hash que recibió.
  Pégalo en Meta tal cual.

### 5.7 Client Token
- [ ] **Configuración de la app → Avanzada (Advanced)** → busca **Token de cliente (Client token)** → cópialo y anótalo.

### 5.8 Pegar los datos en el proyecto
- [ ] En Android Studio abre `app\src\main\res\values\strings.xml` y reemplaza los tres valores:

```xml
<string name="facebook_app_id">TU_APP_ID</string>
<string name="facebook_client_token">TU_CLIENT_TOKEN</string>
<string name="fb_login_protocol_scheme">fbTU_APP_ID</string>
```

  La tercera es **`fb` pegado al App ID, sin espacio**. Ejemplo: App ID `123456789` → `fb123456789`.

### 5.9 Modo desarrollo
La app de Meta nace en **modo Desarrollo** y está bien así para el laboratorio. Solo pueden entrar las cuentas
con rol en la app (tu cuenta ya es Administrador). No hace falta publicarla ni pasar revisión.
Para probar con otra cuenta: **Roles de la app → Roles → Agregar personas → Probadores**.

---

## Paso 6. Crear el emulador y correr la app

- [ ] Android Studio → **Device Manager** (ícono del teléfono, barra derecha) → **Create Device**.
- [ ] Elige un teléfono que tenga el **ícono de Play Store** en la columna *Play Store* (por ejemplo Pixel 7) → **Next**.
- [ ] Imagen del sistema: **API 34** ("UpsideDownCake"), la que tenga **Google Play**. Descárgala con el ícono de flecha si hace falta → **Next → Finish**.
- [ ] Arranca el emulador (triángulo de Device Manager) y espera a que cargue el Android.
- [ ] En el emulador, abre **Chrome** y entra a `facebook.com`. Inicia sesión una vez con tu cuenta.
  Así el login de Facebook de la app suele ser más rápido.
- [ ] Arriba de Android Studio elige el emulador en el selector de dispositivos y pulsa **Run** (triángulo verde).

**Si el emulador no arranca (virtualización):**
Panel de control → **Programas → Activar o desactivar las características de Windows** → marca
**Windows Hypervisor Platform** y **Plataforma de máquina virtual** → acepta y reinicia.
Si sigue fallando, la virtualización está apagada en la BIOS. Como alternativa, usa un teléfono real:
activa *Opciones de desarrollador → Depuración USB* y conéctalo.

---

## Paso 7. Lista de pruebas (hazla completa antes de la prueba)

- [ ] Se ve la pantalla de inicio en español con el logo y tres botones.
- [ ] **Registrarse** con un correo y contraseña de 6 o más caracteres → entra a la pantalla de registros.
- [ ] **Salir** → **Iniciar sesión** con ese mismo correo → entra.
- [ ] **¿Olvidaste tu contraseña?** → escribe el correo → llega el mensaje (revisa **spam**).
- [ ] **Continuar con Google** → elige tu cuenta → entra.
- [ ] **Continuar con Facebook** → entra.
- [ ] **Agregar** 6 o más registros de distintos tipos. **Editar** uno y **Borrar** otro.
- [ ] En Firebase → **Realtime Database → Datos**: aparece `registros → <uid> → <id>` con tipo, valor y fecha. Los cambios se reflejan.
- [ ] **Gráficos**: salen el gráfico de líneas y el de barras. Cambia el filtro entre Todos / Temperatura / Humedad / Luz y los dos cambian.
- [ ] Cierra la app del todo, ábrela otra vez: sigue con la sesión iniciada y los datos están.

---

## Paso 8. El día del laboratorio

**En una PC distinta tus huellas son distintas.** Es lo que más falla. En esa PC hay que repetir:

1. Paso 2 → obtener su SHA-1 y SHA-256.
2. Firebase → Configuración del proyecto → Tus apps → **Agregar huella digital** (SHA-1 y SHA-256 nuevos).
3. Paso 4.3 → **descargar otra vez** `google-services.json` y reemplazarlo en la carpeta `app`.
4. Paso 5.6 → calcular el hash con ese SHA-1 y agregarlo en Meta → **Hashes de clave** (puede haber varios).
5. Sync y Run.

Si el docente pide **otros datos** (por ejemplo "Presión", "Gas" en lugar de Temperatura, Humedad, Luz):
- Cambia la lista en `app\src\main\java\com\lab6\iot\Registro.java`, línea 5: `TIPOS`.
- Si pide **más campos** (por ejemplo una ubicación): agrégalos en `Registro.java`, en `res\layout\dialog_registro.xml`,
  en `HomeActivity.java` (método `mostrarDialogo`) y en `RegistroAdapter.java` (para mostrarlos).
- Si pide **otro gráfico** (por ejemplo torta): en `ChartsActivity.java`, método `dibujar()`.

Dónde está cada criterio de la rúbrica, por si tienes que arreglar algo rápido:

| Criterio | Archivo |
|---|---|
| 1. Interfaz de inicio | `MainActivity.java`, `res\layout\activity_main.xml`, `res\drawable\ic_logo.xml` |
| 2. Inicio de sesión | `LoginActivity.java` (correo, Google, Facebook), `RegisterActivity.java`, `ResetActivity.java` |
| 3. Firebase Database | `HomeActivity.java` (crear, leer, editar, borrar), `Registro.java`, `RegistroAdapter.java` |
| 4. Gráficos | `ChartsActivity.java`, `res\layout\activity_charts.xml` |
| 5. Funcionalidad | Toda la app. Revisa el Paso 7 completo. |

---

## Errores comunes

| Síntoma | Qué hacer |
|---|---|
| Google: mensaje `Error con Google: 10` o `12500` | Falta el SHA-1 en Firebase, o el `google-services.json` es viejo o no tiene `oauth_client`. Agrega la huella, descarga el JSON otra vez, reemplázalo, Sync y Run. Además, el emulador debe ser uno con Google Play. |
| Google: `Error con Google: 7` | Sin internet en el emulador. |
| Facebook: "Invalid key hash" | Falta el hash en Meta o se calculó con otro SHA-1. Pega el hash que muestra el mensaje de error. |
| Facebook: "App not active" o no deja entrar | La cuenta no tiene rol en la app de Meta (modo desarrollo). Agrégala como Probador. |
| Facebook: abre y se queda en blanco o se cierra | Revisa que `fb_login_protocol_scheme` sea `fb` + App ID y que la URI de redirección esté pegada en Meta. |
| `Permission denied` al guardar | No hay sesión iniciada o las reglas no se publicaron (Paso 4.2). |
| Al crear usuario: "The email address is badly formatted" | El correo está mal escrito. |
| Todo falla con "API key not valid" o aparece `REEMPLAZAR` | Sigues con el `google-services.json` de relleno (Paso 4.3). |
| `gradlew.bat`: "JAVA_HOME is not set" | Ejecuta antes `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"` en la misma ventana de PowerShell. Si Android Studio está en otra ruta: `Get-ChildItem "C:\Program Files" -Recurse -Filter java.exe -ErrorAction SilentlyContinue` |
| `keytool` no se reconoce como comando | Usa la ruta completa de la Opción B del Paso 2, con `&` y las comillas. |
| `Could not find com.github.PhilJay:MPAndroidChart` | Sin internet, o JitPack caído. Reintenta el Sync. |
| El Sync dice "Unsupported class file major version" | Cambia el **Gradle JDK** a jbr-21 (ver Paso 1). |
| "SDK location not found" o falta la plataforma 34 | **Tools → SDK Manager** → instala Android 14 (API 34). |
| El emulador es muy lento o no arranca | Ver Paso 6 (virtualización) o usa un teléfono real. |
