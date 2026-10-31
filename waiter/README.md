# Waiter

App for the waiters of a restaurant that uses DonPos in restaurant mode.
The waiter logs in, chooses the floor (only when there is more than one), the table, the
category and the products, and goes back to the tables. The cashier opens the same table in
DonPos and bills it. It needs the DonPosOrder server running.

## Configuración de DonPos (obligatoria)
Sin estos pasos la app de los meseros funciona, pero la caja no puede cobrar sus mesas.

1. **Modo restaurante:** Configuración → General → Recibos = `restaurant`. Reiniciar DonPos.
2. **Refresco del mapa de mesas:** Configuración → System Options → marcar "Auto Refresh Table
   Map" (por ejemplo cada 3 segundos), para que la caja vea las mesas que ocupan los meseros.
3. **Permiso del cajero:** DonPos solo deja abrir la mesa de otro usuario si el rol tiene este
   permiso. Mantenimiento → Roles → "Employee role" → agregar esta línea junto a las otras
   `<class .../>` y guardar:
   ```xml
   <class name="sales.Override"/>
   ```
   Sin esa línea DonPos dice "Desafortunadamente no puedes verlos" y el cajero no puede abrir la
   mesa del mesero. Se pone en "Employee role" (y no en un rol nuevo) para que también funcione
   con Kent: la tablet del cliente solo lista a los cajeros con ese rol. Los cajeros deben salir
   y volver a entrar a DonPos después del cambio.
4. **Meseros:** usuarios con el rol "Command" (`donpos.waiter.roles` en DonPosOrder).

DonPosOrder necesita Java 25 para compilar y correr.

## Install
```sh
pnpm install
```
## Development
Web version in the browser:
```sh
pnpm expo start --web
```
On the phone, scan the QR code with the Expo Go app (same WiFi as the PC).

## Configure
The first time, write the DonPosOrder address (for example `192.168.1.8:8080`). Only users with
a waiter role (`donpos.waiter.roles` in DonPosOrder) can log in. The server and the session are
saved in SecureStore on the phone (in localStorage on the web), so the waiter stays logged in
until "Cerrar sesión".

## Tables
- Green: free. Blue: mine. Red: taken by another waiter or the cashier. Yellow: the cashier has it
  open in DonPos (billing).
- The tables are shown 6 by 6 and refreshed every 5 seconds.
- Inside a table the app sends a signal every 30 seconds. If the phone stops sending it for
  2 minutes (`donpos.waiter.lock-timeout`), DonPosOrder releases the lock of the table.
- A table left without products is released when the waiter goes back.
