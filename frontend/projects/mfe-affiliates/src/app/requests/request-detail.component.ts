import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SubsidyRequestDetail } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';
import { StatusStreamService } from './status-stream.service';

const TYPE_LABELS: Record<string, string> = {
  VIVIENDA: 'Vivienda',
  EDUCACION: 'Educación',
  RECREACION: 'Recreación',
  CREDITO: 'Crédito',
};

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  SUBMITTED: 'Radicada',
  PENDING_DOCUMENTS: 'Pendiente de documentos',
  IN_REVIEW: 'En revisión',
  APPROVED_DEMO: 'Aprobada (demostración)',
  REJECTED_DEMO: 'Rechazada (demostración)',
  CANCELLED: 'Cancelada',
};

const DOCUMENT_STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pendiente',
  RECEIVED: 'Recibido',
  VALIDATED: 'Validado',
  REJECTED: 'Rechazado',
};

const DOCUMENT_LABELS: Record<string, string> = {
  ID_DOCUMENT: 'Documento de identidad',
  INCOME_CERTIFICATE: 'Certificado de ingresos',
  BANK_CERTIFICATE: 'Certificado bancario',
  ENROLLMENT_CERTIFICATE: 'Certificado de matrícula',
  FAMILY_COMPOSITION_CERTIFICATE: 'Certificado de composición familiar',
};

/**
 * Detalle de una solicitud con su línea de tiempo en vivo (UC-05).
 *
 * La página carga el detalle una vez y luego abre el stream SSE. Cuando llega un cambio de estado vuelve a
 * pedir el detalle en lugar de mezclar el evento con la vista: el evento solo dice qué estado, y recargar
 * garantiza que el historial, los documentos y los contadores sigan siendo consistentes entre sí.
 *
 * El stream reenvía el historial cuando no hay `Last-Event-ID`, así que la primera conexión entrega eventos
 * viejos. Para no recargar varias veces ni anunciar "actualizado" por algo que ya estaba en pantalla, cada
 * evento se compara con el instante del último registro del detalle: solo los posteriores cuentan como cambio.
 */
@Component({
  selector: 'cs-request-detail',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './request-detail.component.html',
  styleUrl: './request-detail.component.scss',
})
export class RequestDetailComponent implements OnInit {
  private readonly api = inject(AffiliateApiService);
  private readonly stream = inject(StatusStreamService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly requestId = Number(this.route.snapshot.paramMap.get('id'));
  readonly loading = signal(true);
  readonly detail = signal<SubsidyRequestDetail | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly liveMessage = signal<string | null>(null);

  /** Instante del último evento ya reflejado en el detalle; evita recargar por el replay inicial. */
  private lastKnownAt = 0;

  ngOnInit(): void {
    this.load();
    this.follow();
  }

  /** Carga el detalle; en recargas conserva lo que ya está en pantalla mientras llega la respuesta. */
  load(): void {
    if (!this.detail()) {
      this.loading.set(true);
    }
    this.errorMessage.set(null);
    this.api.getRequestDetail(this.requestId).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        const history = detail.history;
        this.lastKnownAt = history.length > 0
          ? new Date(history[history.length - 1].occurredAt).getTime()
          : 0;
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.messageFor(error));
      },
    });
  }

  /** Se suscribe al stream y recarga el detalle cuando el estado cambia de verdad. */
  private follow(): void {
    this.stream
      .stream(this.requestId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        const current = this.detail();
        if (!current || current.status === event.status) {
          return;
        }
        if (new Date(event.occurredAt).getTime() <= this.lastKnownAt) {
          return;
        }
        this.liveMessage.set(`Estado actualizado a ${this.statusLabel(event.status)}.`);
        this.load();
      });
  }

  typeLabel(type: string): string {
    return TYPE_LABELS[type] ?? type;
  }

  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  documentStatusLabel(status: string): string {
    return DOCUMENT_STATUS_LABELS[status] ?? status;
  }

  documentLabel(type: string): string {
    return DOCUMENT_LABELS[type] ?? type;
  }

  private messageFor(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return 'Tu sesión expiró. Vuelve a ingresar.';
    }
    if (error.status === 403) {
      return 'No tienes permiso para consultar esta solicitud.';
    }
    if (error.status === 404) {
      return 'La solicitud no existe o no está disponible.';
    }
    return 'No fue posible cargar la solicitud. Intenta de nuevo.';
  }
}
