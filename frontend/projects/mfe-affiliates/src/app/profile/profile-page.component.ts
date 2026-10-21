import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AffiliateProfile, SubsidyRequestSummary } from 'shared-core';
import { AffiliateApiService } from './affiliate-api.service';

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  SUBMITTED: 'Radicada',
  PENDING_DOCUMENTS: 'Documentos pendientes',
  IN_REVIEW: 'En revisión',
  APPROVED_DEMO: 'Aprobada (demostración)',
  REJECTED_DEMO: 'Rechazada (demostración)',
  CANCELLED: 'Cancelada',
};

const TYPE_LABELS: Record<string, string> = {
  VIVIENDA: 'Vivienda',
  EDUCACION: 'Educación',
  RECREACION: 'Recreación',
  CREDITO: 'Crédito',
};

/**
 * Pantalla de perfil del afiliado (UC-01).
 *
 * Carga el perfil y las últimas solicitudes en dos llamadas independientes: si una falla, la otra sigue
 * visible, porque un error en el listado no debe impedir ver los datos personales. La vista tiene tres
 * estados explícitos —cargando, error y contenido— y anuncia los cambios con `aria-busy` para que un
 * lector de pantalla sepa cuándo terminó la carga.
 *
 * El número de documento llega ya enmascarado desde la API; el componente no lo manipula, de modo que un
 * cambio en la política de enmascarado no exige tocar el frontend.
 */
@Component({
  selector: 'cs-profile-page',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.scss',
})
export class ProfilePageComponent implements OnInit {
  private readonly api = inject(AffiliateApiService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly profile = signal<AffiliateProfile | null>(null);
  readonly requests = signal<SubsidyRequestSummary[]>([]);

  /** Traduce el estado del backend sin acoplar la vista a los nombres internos del enum. */
  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  /** Traduce el tipo de subsidio para mostrarlo en la tabla de solicitudes. */
  typeLabel(type: string): string {
    return TYPE_LABELS[type] ?? type;
  }

  ngOnInit(): void {
    this.load();
  }

  /**
   * Recarga el perfil y las solicitudes.
   *
   * Cada llamada gestiona su propio error: el perfil es obligatorio para pintar la pantalla, mientras que
   * el listado puede fallar sin bloquearla. El mensaje distingue entre sesión inválida y fallo de red,
   * que son las dos causas que un usuario puede encontrar.
   */
  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.api.getProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          error.status === 401
            ? 'Tu sesión expiró. Vuelve a ingresar.'
            : 'No fue posible cargar tu perfil. Intenta de nuevo.',
        );
      },
    });

    this.api.getRecentRequests().subscribe({
      next: (page) => this.requests.set(page.content),
      error: () => this.requests.set([]),
    });
  }
}
