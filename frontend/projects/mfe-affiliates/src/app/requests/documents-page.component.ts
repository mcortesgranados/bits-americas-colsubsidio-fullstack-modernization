import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SubsidyDocument } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';

const DOCUMENT_LABELS: Record<string, string> = {
  ID_DOCUMENT: 'Documento de identidad',
  INCOME_CERTIFICATE: 'Certificado de ingresos',
  BANK_CERTIFICATE: 'Certificado bancario',
  ENROLLMENT_CERTIFICATE: 'Certificado de matrícula',
  FAMILY_COMPOSITION_CERTIFICATE: 'Certificado de composición familiar',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pendiente',
  RECEIVED: 'Recibido',
  VALIDATED: 'Validado',
  REJECTED: 'Rechazado',
};

const ACCEPTED_TYPES = ['application/pdf', 'image/png', 'image/jpeg'];
const MAX_SIZE_BYTES = 5 * 1024 * 1024;

/**
 * Pantalla de evidencias de una solicitud (UC-04).
 *
 * Muestra el catálogo completo con el estado de cada documento y permite cargar o reemplazar el archivo.
 * La validación local —tipo y tamaño— es solo una cortesía para no gastar una petición ni subir 5 MB por una
 * red lenta: el backend vuelve a validar con las mismas reglas y su respuesta manda. Por eso los mensajes de
 * error se construyen a partir del código estable del servidor y no del texto del problem detail.
 *
 * La descarga pasa por `HttpClient` y no por un enlace: el endpoint exige el token en la cabecera, y un
 * `<a href>` no lo enviaría. El blob se convierte en un object URL temporal que se revoca al terminar.
 */
@Component({
  selector: 'cs-documents-page',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './documents-page.component.html',
  styleUrl: './documents-page.component.scss',
})
export class DocumentsPageComponent implements OnInit {
  private readonly api = inject(AffiliateApiService);
  private readonly route = inject(ActivatedRoute);

  readonly requestId = Number(this.route.snapshot.paramMap.get('id'));
  readonly loading = signal(true);
  readonly uploadingType = signal<string | null>(null);
  readonly documents = signal<SubsidyDocument[]>([]);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  /** Carga el catálogo de documentos de la solicitud. */
  load(): void {
    this.loading.set(true);
    this.api.getDocuments(this.requestId).subscribe({
      next: (documents) => {
        this.documents.set(documents);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.messageFor(error));
      },
    });
  }

  /**
   * Valida el archivo en el navegador y lo envía.
   *
   * El input se limpia antes de subir para que elegir el mismo archivo otra vez vuelva a disparar el evento,
   * que es como funciona el reemplazo de un documento ya recibido.
   *
   * @param documentType tipo de documento de la fila
   * @param event evento de cambio del input de archivo
   */
  onFileSelected(documentType: string, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (!ACCEPTED_TYPES.includes(file.type)) {
      this.errorMessage.set('Solo se aceptan archivos PDF, PNG o JPG.');
      return;
    }
    if (file.size > MAX_SIZE_BYTES) {
      this.errorMessage.set('El archivo supera el máximo de 5 MB.');
      return;
    }
    this.upload(documentType, file);
  }

  /** Descarga el archivo del documento ya recibido. */
  download(document: SubsidyDocument): void {
    this.api.downloadDocument(this.requestId, document.documentType).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = window.document.createElement('a');
        anchor.href = url;
        anchor.download = document.fileName ?? `${document.documentType}.pdf`;
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: (error: HttpErrorResponse) => this.errorMessage.set(this.messageFor(error)),
    });
  }

  documentLabel(type: string): string {
    return DOCUMENT_LABELS[type] ?? type;
  }

  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  /** Tamaño legible; el valor llega en bytes. */
  formatSize(bytes: number | null): string {
    if (bytes == null) {
      return '—';
    }
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    return bytes >= 1024 * 1024
      ? `${(bytes / (1024 * 1024)).toFixed(1)} MB`
      : `${Math.round(bytes / 1024)} KB`;
  }

  private upload(documentType: string, file: File): void {
    this.uploadingType.set(documentType);
    this.api.uploadDocument(this.requestId, documentType, file).subscribe({
      next: (uploaded) => {
        this.uploadingType.set(null);
        this.successMessage.set(
          `${this.documentLabel(documentType)} ${uploaded.replaced ? 'reemplazado' : 'cargado'} correctamente.`,
        );
        this.load();
      },
      error: (error: HttpErrorResponse) => {
        this.uploadingType.set(null);
        this.errorMessage.set(this.messageFor(error));
      },
    });
  }

  /** Traduce el código estable del backend; el texto técnico no se muestra. */
  private messageFor(error: HttpErrorResponse): string {
    const code = (error.error as { code?: string } | null)?.code;
    switch (code) {
      case 'FILE_TYPE_NOT_ALLOWED':
        return 'El tipo de archivo no está permitido. Usa PDF, PNG o JPG.';
      case 'FILE_TOO_LARGE':
        return 'El archivo supera el máximo permitido de 5 MB.';
      case 'DOCUMENT_NOT_REQUIRED':
        return 'Ese documento no hace parte del catálogo de esta solicitud.';
      case 'INVALID_STATUS_TRANSITION':
        return 'La solicitud ya está cerrada y no admite más documentos.';
      case 'EMPTY_FILE':
        return 'El archivo está vacío.';
      case 'DOCUMENT_NOT_UPLOADED':
        return 'Ese documento todavía no tiene un archivo cargado.';
      default:
        break;
    }
    if (error.status === 401) {
      return 'Tu sesión expiró. Vuelve a ingresar.';
    }
    if (error.status === 413) {
      return 'El archivo supera el máximo permitido de 5 MB.';
    }
    if (error.status === 403) {
      return 'No tienes permiso para acceder a los documentos de esta solicitud.';
    }
    return 'No fue posible completar la operación. Intenta de nuevo.';
  }
}
