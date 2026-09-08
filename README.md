# Intellij Pets

Adds a cute pet to your favourite IDE.

Inspired (very heavily) by [vscode-pets](https://github.com/tonybaloney/vscode-pets) created by [Anthony Shaw](https://github.com/tonybaloney).

## Features
- A pet that walks around, sits, follows you, interacts with you, basically a fwend
- Pet variants: 5 cats, bunny, chicken, axolotl, hedgehog, 5 dogs, etc.
- Pet scale setting, to adjust the size of your pet
- Five direct interactions: pet, greet, feed, drag, and play ball

## Instalación en IntelliJ IDEA

### Opción 1: JetBrains Marketplace

1. Abre **Settings/Preferences > Plugins > Marketplace**.
2. Busca **Pets** y selecciona **Install**.
3. Reinicia el IDE si IntelliJ lo solicita.
4. Pulsa el icono de mascota en la barra lateral derecha para abrir la ventana **Pets**.

### Opción 2: instalar un ZIP local

1. Descarga el archivo ZIP de una versión publicada o genera uno siguiendo la opción 3.
2. Abre **Settings/Preferences > Plugins**.
3. En el menú de engranaje, elige **Install Plugin from Disk...**.
4. Selecciona el ZIP **sin descomprimirlo**, acepta la instalación y reinicia el IDE.

### Opción 3: ejecutar o compilar desde el código fuente

Requisitos: Git y JDK 17. El Gradle Wrapper descarga el resto de las herramientas.

```bash
git clone <URL-del-repositorio>
cd intellij-pets
./gradlew runIde
```

`runIde` abre una instancia aislada de IntelliJ para probar el plugin. Para generar un
ZIP instalable localmente:

```bash
./gradlew clean test buildPlugin
```

El artefacto se crea en `build/distributions/`. Los sprites comerciales (`*.png`) no
se versionan por su licencia; si no están disponibles, el plugin usa una figura de
respaldo generada por código para que el desarrollo y las pruebas sigan funcionando.

El plugin requiere IntelliJ Platform build 232 o posterior (IntelliJ IDEA 2023.2+).

## Uso e interacciones

- After installation, you'll find a pet icon on the right toolbar of your IDE, click on it to open the tool window
- To change the pet variant, go to Preferences > Tools > Pets, and select a pet variant (see screenshot below)
- Usa **Add Pet** (`+`) y **Remove Pet** (`−`) en la barra de la ventana para administrar la colección.
- Cambia el tamaño desde **Settings/Preferences > Tools > Pets**.

Las cinco interacciones disponibles son:

| Interacción | Gesto | Animación |
| --- | --- | --- |
| Acariciar | Clic izquierdo sobre una mascota | La mascota reacciona y aparecen corazones |
| Saludar | Doble clic sobre una mascota | Salta rodeada de destellos |
| Alimentar | Clic derecho sobre una mascota | Aparece un premio y la mascota come |
| Reubicar | Arrastrar una mascota | Corre durante el arrastre y se sienta al soltarla |
| Jugar | Clic izquierdo en un espacio vacío | Aparece una pelota que rebota y la mascota más cercana la persigue |

Cuando hay mascotas superpuestas, el gesto se aplica a la que se dibuja en primer plano.

## Seguridad

La revisión de seguridad y sus limitaciones están documentadas en
[`SECURITY_AUDIT.md`](./SECURITY_AUDIT.md). Las variantes y escalas persistidas se
validan antes de acceder a recursos, las animaciones se ejecutan en el hilo de Swing y
el temporizador se detiene al cerrar la ventana. Dependabot revisa semanalmente las
dependencias Gradle.

## Credits

- Original idea from [vscode-pets](https://github.com/tonybaloney/vscode-pets) created by [Anthony Shaw](https://github.com/tonybaloney)
- All pet spritesheets created by [SeethingSwarm](https://seethingswarm.itch.io/) (note: the assets were purchased, must not be resold, see itch.io page for details)

## Screenshots
Collection ![Collection](./screenshots/collection.png?raw=true "Collection")

Demo 1 ![Demo](./screenshots/demo-1.png?raw=true "Demo")

Demo 2 ![Demo](./screenshots/demo-2.png?raw=true "Demo")

Settings ![Settings](./screenshots/settings.png?raw=true "Settings")
