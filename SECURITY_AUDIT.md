# Revisión de seguridad

Fecha de revisión: 2026-09-08

## Alcance y metodología

Se revisaron manualmente el código Kotlin, `plugin.xml`, la configuración Gradle, el
Gradle Wrapper y los recursos versionados. También se buscaron patrones de secretos
(claves privadas, tokens comunes y asignaciones de contraseñas) en todos los archivos
fuera de `.git`.

El plugin no procesa contenido remoto, no abre sockets, no ejecuta comandos, no usa
bases de datos y no solicita permisos adicionales del IDE. Los únicos secretos
referenciados por el build (`PRIVATE_KEY`, `CERTIFICATE_CHAIN` y `PUBLISH_TOKEN`) se
leen de variables de entorno y no se almacenan en el repositorio.

## Resultado

No se encontraron credenciales versionadas ni una vía conocida para ejecución de
código, acceso arbitrario a archivos o exposición de datos desde la funcionalidad del
plugin. Sí se encontraron cuatro problemas locales de disponibilidad/estabilidad que
podían bloquear la ventana o conservar recursos; todos quedaron mitigados:

| Severidad | Hallazgo | Mitigación |
| --- | --- | --- |
| Media | Un `java.util.Timer` sin cancelar sobrevivía al cierre de la ventana y modificaba estado Swing fuera del Event Dispatch Thread. | Se reemplazó por `javax.swing.Timer` y se enlazó su parada al `Disposable` del contenido. |
| Media | Una escala, variante o lista persistida manipulada podía producir tamaños inválidos o forzar accesos a recursos inexistentes. | Se normalizan variantes y escala al cargar y antes de crear mascotas. |
| Baja | Un panel más estrecho que el sprite podía pasar un límite no positivo al generador aleatorio y detener la animación. | Todos los movimientos se limitan a un rango seguro, incluido el panel con ancho cero. |
| Baja | Un spritesheet ausente o ilegible provocaba un fallo durante la creación del Tool Window. | La carga se encapsuló y utiliza un frame de respaldo generado localmente. |

## Dependencias y cadena de suministro

- Gradle Wrapper apunta a Gradle 8.7 mediante HTTPS.
- El build usa Kotlin 1.9.23 e IntelliJ Platform Gradle Plugin 1.17.3.
- Se añadió Dependabot semanal para detectar futuras actualizaciones de Gradle.
- `gradle-wrapper.jar` está versionado, como recomienda el flujo habitual del Wrapper.

La comprobación automática contra bases públicas de CVE **no pudo completarse** en el
entorno de revisión: el proxy devolvió HTTP 403 al descargar la distribución y el
servicio de búsqueda devolvió HTTP 401. Por ello este informe no afirma que las
versiones del toolchain estén libres de CVE. Antes de publicar se debe ejecutar, en un
entorno con red:

```bash
./gradlew dependencies
./gradlew buildPlugin verifyPlugin
```

Además, se recomienda habilitar las alertas de seguridad y Dependabot en GitHub y
revisar cada actualización con las notas oficiales de Gradle, Kotlin y JetBrains.

## Riesgo residual

Los sprites originales son assets comerciales ignorados por Git. Un distribuidor debe
obtenerlos de una fuente autorizada y verificar su integridad antes de empaquetarlos.
La figura de respaldo elimina el fallo por ausencia, pero no valida archivos PNG que
un tercero coloque localmente en el árbol de recursos.
