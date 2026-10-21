import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

/**
 * Pantalla de respaldo cuando un microfrontend no se puede cargar.
 *
 * El shell sigue siendo usable: la cabecera, la navegación y el resto de rutas funcionan aunque un remoto
 * esté caído o su contrato no coincida. El motivo se muestra en texto para que el fallo sea diagnosticable
 * sin abrir la consola.
 */
@Component({
  selector: 'cs-remote-unavailable',
  standalone: true,
  template: `
    <section class="state" aria-labelledby="remote-title">
      <h1 id="remote-title">Este portal no está disponible</h1>
      <p>
        El microfrontend no respondió o su versión no es compatible con la del contenedor. El resto del
        portal sigue funcionando.
      </p>
      @if (reason) {
        <pre class="state__reason">{{ reason }}</pre>
      }
    </section>
  `,
  styles: [
    `
      .state {
        max-width: 36rem;
        margin: 2rem auto;
        padding: 1.5rem;
        border: 1px solid #d7a3a5;
        border-radius: 0.75rem;
        background: #fdf3f3;
      }
      .state__reason {
        margin: 1rem 0 0;
        padding: 0.75rem;
        overflow-x: auto;
        background: #ffffff;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        white-space: pre-wrap;
        color: #7a1114;
      }
    `,
  ],
})
export class RemoteUnavailableComponent {
  readonly reason = inject(ActivatedRoute).snapshot.data['reason'] as string | undefined;
}
