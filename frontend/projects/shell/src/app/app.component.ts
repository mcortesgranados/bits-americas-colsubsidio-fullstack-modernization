import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

/**
 * Marco del portal: cabecera, navegación y contenedor de rutas.
 *
 * Es lo único que el usuario ve siempre, así que aquí viven la identidad del portal y las acciones de
 * sesión. El contenido de cada sección llega por el router, incluido el microfrontend de afiliados.
 */
@Component({
  selector: 'cs-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  /** Cierra la sesión y vuelve al acceso. */
  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
