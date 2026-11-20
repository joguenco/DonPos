# Problemas

## No mostrar al abrir e archivo start.bat
Opción 1: Agregar la unidad Z: como sitio de confianza (Recomendado)

Este método evita el mensaje únicamente para tu unidad de red sin comprometer la seguridad general de tu computadora:
1. Presiona las teclas Windows + R, escribe inetcpl.cpl y presiona Enter para abrir las Opciones de Internet.
2. Ve a la pestaña Seguridad.
3. Selecciona el ícono Intranet local y haz clic en el botón Sitios.
4. En la nueva ventana, haz clic en Opciones avanzadas.
5. En el cuadro "Agregar este sitio web a la zona", escribe file://Z: (o la ruta de red completa, por ejemplo \\nombre-servidor\carpeta) y haz clic en Agregar.
6. Haz clic en Cerrar, luego en Aceptar en las ventanas restantes y vuelve a intentar abrir el archivo.
