import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { SubsidyDocument, SubsidyDocumentUploaded } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';
import { DocumentsPageComponent } from './documents-page.component';

const DOCUMENTS: SubsidyDocument[] = [
  {
    id: 1,
    documentType: 'ID_DOCUMENT',
    status: 'RECEIVED',
    fileName: 'cedula.pdf',
    contentType: 'application/pdf',
    sizeBytes: 2048,
    checksum: 'abc',
    uploadedAt: '2026-10-21T15:30:00Z',
  },
  {
    id: 2,
    documentType: 'INCOME_CERTIFICATE',
    status: 'PENDING',
    fileName: null,
    contentType: null,
    sizeBytes: null,
    checksum: null,
    uploadedAt: null,
  },
];

const UPLOADED: SubsidyDocumentUploaded = {
  ...DOCUMENTS[1],
  status: 'RECEIVED',
  fileName: 'ingresos.pdf',
  contentType: 'application/pdf',
  sizeBytes: 1024,
  checksum: 'def',
  uploadedAt: '2026-10-21T16:00:00Z',
  replaced: false,
};

/**
 * Pruebas de la pantalla de documentos (UC-04).
 *
 * Cubren la lista con estados, la carga de un archivo válido, el rechazo local de un tipo no permitido —que no
 * debe llegar a la API— y la traducción del código `FILE_TOO_LARGE` cuando el servidor rechaza el tamaño.
 */
describe('DocumentsPageComponent', () => {
  let getDocuments: jest.Mock;
  let uploadDocument: jest.Mock;
  let downloadDocument: jest.Mock;

  async function setup(
    documents: Observable<SubsidyDocument[]>,
    upload: Observable<SubsidyDocumentUploaded> = of(UPLOADED),
  ) {
    getDocuments = jest.fn(() => documents);
    uploadDocument = jest.fn(() => upload);
    downloadDocument = jest.fn(() => of(new Blob(['x'])));

    await TestBed.configureTestingModule({
      imports: [DocumentsPageComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '5' } } } },
        {
          provide: AffiliateApiService,
          useValue: { getDocuments, uploadDocument, downloadDocument },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DocumentsPageComponent);
    fixture.detectChanges();
    return fixture;
  }

  function selectFile(fixture: ComponentFixture<DocumentsPageComponent>, file: File): void {
    fixture.componentInstance.onFileSelected('INCOME_CERTIFICATE', {
      target: { files: [file], value: 'ingresos.pdf' },
    } as unknown as Event);
    fixture.detectChanges();
  }

  it('lists the documents with their labels, status and file metadata', async () => {
    const fixture = await setup(of(DOCUMENTS));

    const text = fixture.nativeElement.textContent ?? '';

    expect(text).toContain('Documento de identidad');
    expect(text).toContain('Certificado de ingresos');
    expect(text).toContain('Recibido');
    expect(text).toContain('Pendiente');
    expect(text).toContain('cedula.pdf');
    expect(text).toContain('2 KB');
  });

  it('formats small files in bytes and larger ones in kilobytes', async () => {
    const fixture = await setup(of(DOCUMENTS));

    expect(fixture.componentInstance.formatSize(68)).toBe('68 B');
    expect(fixture.componentInstance.formatSize(2048)).toBe('2 KB');
  });

  it('uploads a valid file and confirms the result', async () => {
    const fixture = await setup(of(DOCUMENTS));
    const file = new File(['contenido'], 'ingresos.pdf', { type: 'application/pdf' });

    selectFile(fixture, file);

    expect(uploadDocument).toHaveBeenCalledWith(5, 'INCOME_CERTIFICATE', file);
    expect(fixture.nativeElement.textContent ?? '').toContain('Certificado de ingresos cargado correctamente.');
  });

  it('rejects a non accepted type without calling the API', async () => {
    const fixture = await setup(of(DOCUMENTS));
    const file = new File(['x'], 'notas.txt', { type: 'text/plain' });

    selectFile(fixture, file);

    expect(uploadDocument).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent ?? '').toContain('Solo se aceptan archivos PDF, PNG o JPG.');
  });

  it('explains a server side size rejection', async () => {
    const fixture = await setup(
      of(DOCUMENTS),
      throwError(() => new HttpErrorResponse({ status: 413, error: { code: 'FILE_TOO_LARGE' } })),
    );
    const file = new File(['x'], 'ingresos.pdf', { type: 'application/pdf' });

    selectFile(fixture, file);

    expect(fixture.nativeElement.textContent ?? '').toContain('supera el máximo permitido de 5 MB');
  });
});
