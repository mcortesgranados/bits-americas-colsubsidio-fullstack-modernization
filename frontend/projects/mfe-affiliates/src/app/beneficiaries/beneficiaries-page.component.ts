import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Beneficiary } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';

/** Traducciones del parentesco; el backend envía el código del enum. */
const RELATIONSHIP_LABELS: Record<string, string> = {
  HIJO: 'Hijo/a',
  CONYUGE: 'Cónyuge',
  COMPANERO_PERMANENTE: 'Compañero/a permanente',
  PADRE: 'Padre',
  MADRE: 'Madre',
  HERMANO: 'Hermano/a',
};

/** Traducciones del estado del beneficiario. */
const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
};

/**
 * Pantalla de beneficiarios del afiliado (UC-02).
 *
 * La API devuelve la lista ya ordenada por nombre y con el documento enmascarado; el componente no
 * reordena ni manipula el número, de modo que un cambio en la política de enmascarado no exige tocar la
 * vista. La pantalla distingue tres estados —cargando, error y contenido— y un cuarto caso explícito: el
 * afiliado sin beneficiarios, que no es un error sino un resultado vacío con explicación (flujo alterno A1
 * del caso de uso).
 */
@Component({
  selector: 'cs-beneficiaries-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './beneficiaries-page.component.html',
  styleUrl: './beneficiaries-page.component.scss',
})
export class BeneficiariesPageComponent implements OnInit {
  private readonly api = inject(AffiliateApiService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly beneficiaries = signal<Beneficiary[]>([]);

  /** Traduce el parentesco sin acoplar la vista a los nombres internos del enum. */
  relationshipLabel(relationship: string): string {
    return RELATIONSHIP_LABELS[relationship] ?? relationship;
  }

  /** Traduce el estado del beneficiario para la columna correspondiente. */
  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  ngOnInit(): void {
    this.load();
  }

  /**
   * Carga la lista de beneficiarios.
   *
   * Cada código de error tiene un mensaje propio porque la acción del usuario es distinta: una sesión
   * vencida se resuelve volviendo a ingresar, un 403 no se resuelve reintentando y un fallo de red sí.
   */
  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.api.getBeneficiaries().subscribe({
      next: (beneficiaries) => {
        this.beneficiaries.set(beneficiaries);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.beneficiaries.set([]);
        this.loading.set(false);
        this.errorMessage.set(this.messageFor(error));
      },
    });
  }

  /** Mensaje mostrado al usuario según el estado HTTP de la respuesta. */
  private messageFor(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return 'Tu sesión expiró. Vuelve a ingresar.';
    }
    if (error.status === 403) {
      return 'No tienes permiso para consultar estos beneficiarios.';
    }
    return 'No fue posible cargar tus beneficiarios. Intenta de nuevo.';
  }
}
