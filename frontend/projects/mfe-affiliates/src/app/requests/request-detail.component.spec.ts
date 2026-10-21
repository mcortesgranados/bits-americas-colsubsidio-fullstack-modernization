import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { Subject, of } from 'rxjs';
import { StatusChangedEvent, SubsidyRequestDetail } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';
import { StatusStreamService } from './status-stream.service';
import { RequestDetailComponent } from './request-detail.component';

const DETAIL: SubsidyRequestDetail = {
  id: 5,
  requestNumber: 'SOL-2026-0005',
  subsidyType: 'VIVIENDA',
  status: 'PENDING_DOCUMENTS',
  requestedAmount: 4000000,
  currency: 'COP',
  purpose: 'Arriendo',
  submittedAt: '2026-10-21T15:00:00Z',
  decidedAt: null,
  decisionReason: null,
  pendingDocuments: 2,
  rejectedDocuments: 0,
  documents: [
    {
      id: 1,
      documentType: 'ID_DOCUMENT',
      status: 'RECEIVED',
      fileName: 'cedula.pdf',
      contentType: 'application/pdf',
      sizeBytes: 1024,
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
  ],
  history: [
    {
      id: 9,
      fromStatus: 'DRAFT',
      toStatus: 'SUBMITTED',
      reason: 'Solicitud registrada por el afiliado',
      actor: 'afiliado.demo',
      occurredAt: '2026-10-21T15:00:00Z',
      correlationId: 'corr-1',
    },
  ],
};

/**
 * Pruebas del detalle de la solicitud (UC-05).
 *
 * El stream se sustituye por un `Subject` para poder empujar un cambio de estado en el momento que la prueba
 * controla. Se verifica lo que el caso de uso promete: el detalle con documentos e historial, el aviso de
 * pendientes y que un evento con estado distinto recarga la vista y se anuncia por la región `aria-live`.
 */
describe('RequestDetailComponent', () => {
  let getRequestDetail: jest.Mock;
  let events: Subject<StatusChangedEvent>;

  async function setup() {
    events = new Subject<StatusChangedEvent>();
    getRequestDetail = jest.fn(() => of(DETAIL));

    await TestBed.configureTestingModule({
      imports: [RequestDetailComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '5' } } } },
        { provide: AffiliateApiService, useValue: { getRequestDetail } },
        { provide: StatusStreamService, useValue: { stream: () => events.asObservable() } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(RequestDetailComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('renders the request, its documents and its timeline', async () => {
    const fixture = await setup();

    const text = fixture.nativeElement.textContent ?? '';
    expect(text).toContain('SOL-2026-0005');
    expect(text).toContain('Pendiente de documentos');
    expect(text).toContain('Documento de identidad');
    expect(text).toContain('Recibido');
    expect(text).toContain('Solicitud registrada por el afiliado');
    expect(text).toContain('afiliado.demo');
  });

  it('states how many documents are pending', async () => {
    const fixture = await setup();

    expect(fixture.nativeElement.textContent ?? '').toContain('Tienes 2 documento(s) pendiente(s)');
  });

  it('reloads the detail and announces the change when a new status arrives', async () => {
    const fixture = await setup();
    expect(getRequestDetail).toHaveBeenCalledTimes(1);

    events.next({ requestId: 5, fromStatus: 'PENDING_DOCUMENTS', status: 'IN_REVIEW', occurredAt: '2026-10-21T16:00:00Z' });
    fixture.detectChanges();

    expect(getRequestDetail).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent ?? '').toContain('Estado actualizado a En revisión.');
  });

  it('ignores a replayed event older than the current snapshot', async () => {
    const fixture = await setup();

    events.next({ requestId: 5, fromStatus: 'DRAFT', status: 'SUBMITTED', occurredAt: '2026-10-21T14:00:00Z' });
    fixture.detectChanges();

    expect(getRequestDetail).toHaveBeenCalledTimes(1);
  });

  it('ignores an event that repeats the current status', async () => {
    const fixture = await setup();

    events.next({ requestId: 5, fromStatus: 'SUBMITTED', status: 'PENDING_DOCUMENTS', occurredAt: '2026-10-21T16:00:00Z' });
    fixture.detectChanges();

    expect(getRequestDetail).toHaveBeenCalledTimes(1);
    expect(fixture.nativeElement.textContent ?? '').not.toContain('Estado actualizado');
  });
});
