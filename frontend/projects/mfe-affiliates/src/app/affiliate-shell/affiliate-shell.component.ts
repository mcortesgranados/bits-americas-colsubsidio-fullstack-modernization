import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * Contenedor de navegación del portal de afiliados.
 *
 * Vive dentro del remoto a propósito: el shell no conoce las pantallas del portal, solo el punto de
 * montaje `/afiliados`. Añadir una sección es añadir un enlace aquí y una ruta hija en `entry.routes.ts`;
 * ni el manifiesto ni el shell cambian. Los enlaces son relativos para que el remoto funcione igual
 * montado por el shell o servido solo durante el desarrollo.
 */
@Component({
  selector: 'cs-affiliate-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './affiliate-shell.component.html',
  styleUrl: './affiliate-shell.component.scss',
})
export class AffiliateShellComponent {}
