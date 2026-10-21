import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SubsidyRequestCreated } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';

/** Límites de demostración por tipo, espejo de `SubsidyDemoRules` en el backend. */
const DEMO_LIMITS: Record<string, number> = {
  VIVIENDA: 4500000,
  EDUCACION: 1800000,
  RECREACION: 900000,
  CREDITO: 3000000,
};

const TYPE_LABELS: Record<string, string> = {
  VIVIENDA: 'Vivienda',
  EDUCACION: 'Educación',
  RECREACION: 'Recreación',
  CREDITO: 'Crédito',
};

const DOCUMENT_LABELS: Record<string, string> = {
  ID_DOCUMENT: 'Documento de identidad',
  INCOME_CERTIFICATE: 'Certificado de ingresos',
  BANK_CERTIFICATE: 'Certificado bancario',
  ENROLLMENT_CERTIFICATE: 'Certificado de matrícula',
  FAMILY_COMPOSITION_CERTIFICATE: 'Certificado de composición familiar',
};

/**
 * Asistente para registrar una solicitud de subsidio (UC-03).
 *
 * El formulario solo envía tipo, monto y motivo; el afiliado y el radicado los resuelve el backend a partir
 * del token. La vista tiene dos estados excluyentes —formulario y confirmación— y traduce los códigos
 * estables del backend (`AMOUNT_ABOVE_DEMO_LIMIT`, `NO_BENEFICIARIES`, `CATEGORY_NOT_ELIGIBLE`,
 * `DUPLICATE_OPEN_REQUEST`) a mensajes accionables, en lugar de mostrar el texto técnico del problem detail.
 *
 * Los límites que se muestran como ayuda son los mismos que valida el backend, pero se declaran aquí solo
 * para orientar al usuario: la fuente de verdad sigue siendo `SubsidyDemoRules`, y un cambio allí sin tocar
 * este archivo no abre una brecha de validación, porque el servidor rechaza el monto igual.
 */
@Component({
  selector: 'cs-subsidy-request-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe, DatePipe],
  templateUrl: './subsidy-request-page.component.html',
  styleUrl: './subsidy-request-page.component.scss',
})
export class SubsidyRequestPageComponent {
  private readonly api = inject(AffiliateApiService);
  private readonly fb = inject(FormBuilder);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly created = signal<SubsidyRequestCreated | null>(null);

  readonly form = this.fb.group({
    subsidyType: ['VIVIENDA', Validators.required],
    requestedAmount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    purpose: ['', [Validators.maxLength(400)]],
  });

  readonly limits = DEMO_LIMITS;

  /** Límite vigente según el tipo elegido; se usa como ayuda junto al campo de monto. */
  currentLimit(): number {
    return DEMO_LIMITS[this.form.controls.subsidyType.value ?? ''] ?? 0;
  }

  typeLabel(type: string): string {
    return TYPE_LABELS[type] ?? type;
  }

  documentLabel(document: string): string {
    return DOCUMENT_LABELS[document] ?? document;
  }

  /**
   * Envía el formulario.
   *
   * Si el formulario es inválido se marcan los campos como tocados para que los mensajes aparezcan, y no se
   * llama a la API: no tiene sentido gastar una petición en un dato que el navegador ya sabe inválido. El
   * doble envío se bloquea con la señal `submitting`, que además deshabilita el botón.
   */
  submit(): void {
    if (this.submitting()) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.api.createSubsidyRequest({
      subsidyType: value.subsidyType ?? 'VIVIENDA',
      requestedAmount: Number(value.requestedAmount),
      purpose: value.purpose?.trim() ? value.purpose.trim() : null,
    }).subscribe({
      next: (created) => {
        this.created.set(created);
        this.submitting.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(this.messageFor(error));
      },
    });
  }

  /** Vuelve al formulario en blanco para registrar otra solicitud. */
  restart(): void {
    this.created.set(null);
    this.errorMessage.set(null);
    this.form.reset({ subsidyType: 'VIVIENDA', requestedAmount: null, purpose: '' });
  }

  /** Traduce el código estable del backend; el texto del problem detail no se muestra al usuario. */
  private messageFor(error: HttpErrorResponse): string {
    const code = (error.error as { code?: string } | null)?.code;
    switch (code) {
      case 'AMOUNT_ABOVE_DEMO_LIMIT':
        return `El monto supera el límite de demostración para ${this.typeLabel(this.form.controls.subsidyType.value ?? '')}: ${this.currentLimit()} COP.`;
      case 'NO_BENEFICIARIES':
        return 'Educación exige al menos un beneficiario registrado. Registra uno antes de continuar.';
      case 'CATEGORY_NOT_ELIGIBLE':
        return 'El crédito solo está disponible para afiliados de categoría A o B.';
      case 'DUPLICATE_OPEN_REQUEST':
        return 'Ya tienes una solicitud abierta de este tipo. Consulta su estado en Mi perfil.';
      case 'AFFILIATE_NOT_ACTIVE':
        return 'Tu afiliación no está activa, así que no puedes registrar solicitudes.';
      case 'VALIDATION_ERROR':
        return 'Revisa los datos: hay campos con formato inválido.';
      default:
        break;
    }
    if (error.status === 401) {
      return 'Tu sesión expiró. Vuelve a ingresar.';
    }
    if (error.status === 403) {
      return 'Tu usuario no tiene permiso para registrar solicitudes.';
    }
    return 'No fue posible registrar la solicitud. Intenta de nuevo.';
  }
}
