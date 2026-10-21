import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Página mostrada cuando la ruta pedida no existe en el shell. */
@Component({
  selector: 'cs-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="state" aria-labelledby="not-found-title">
      <h1 id="not-found-title">No encontramos esa página</h1>
      <p>La dirección que abriste no corresponde a ninguna sección del portal.</p>
      <a class="state__action" routerLink="/afiliados">Ir al portal de afiliados</a>
    </section>
  `,
  styles: [
    `
      .state {
        max-width: 32rem;
        margin: 3rem auto;
        padding: 1.5rem;
        text-align: center;
      }
      .state__action {
        display: inline-block;
        margin-top: 1rem;
        color: #14603a;
        font-weight: 600;
      }
    `,
  ],
})
export class NotFoundComponent {}
