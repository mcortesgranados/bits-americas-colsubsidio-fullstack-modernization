import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AffiliateProfile, PageResponse, SubsidyRequestSummary } from 'shared-core';
import { AffiliateApiService } from './affiliate-api.service';
import { ProfilePageComponent } from './profile-page.component';

const PROFILE: AffiliateProfile = {
  id: 1,
  fullName: 'Ana María Rojas Peña',
  documentType: 'CC',
  documentNumber: '******4050',
  email: 'ana.rojas@example.test',
  phone: '3001234567',
  category: 'A',
  status: 'ACTIVE',
  companyId: 1,
  companyName: 'Empresa Demo Andina S.A.S.',
  createdAt: '2026-09-03T09:00:00Z',
  updatedAt: '2026-09-03T09:00:00Z',
};

const REQUESTS: PageResponse<SubsidyRequestSummary> = {
  content: [
    {
      id: 5,
      requestNumber: 'SOL-2026-0001',
      subsidyType: 'EDUCACION',
      status: 'IN_REVIEW',
      requestedAmount: 2500000,
      currency: 'COP',
      submittedAt: '2026-10-02T19:00:00Z',
      decidedAt: null,
    },
  ],
  page: 0,
  size: 5,
  totalElements: 1,
  totalPages: 1,
};

/**
 * Pruebas de la pantalla de perfil.
 *
 * El servicio de API se sustituye por un doble que devuelve respuestas fijas, de modo que la prueba
 * describe qué se pinta y no cómo se obtiene. Se comprueba el nombre completo, el documento enmascarado y
 * la traducción del estado, que son los tres puntos donde un cambio de contrato rompe la vista.
 */
describe('ProfilePageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfilePageComponent],
      providers: [
      provideRouter([]),
        {
          provide: AffiliateApiService,
          useValue: {
            getProfile: () => of(PROFILE),
            getRecentRequests: () => of(REQUESTS),
          },
        },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    const fixture = TestBed.createComponent(ProfilePageComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('renders the holder name and the masked document', () => {
    const element = render();
    const text = element.textContent ?? '';

    expect(text).toContain('Ana María Rojas Peña');
    expect(text).toContain('******4050');
    expect(text).toContain('Empresa Demo Andina S.A.S.');
  });

  it('translates the request status to spanish', () => {
    const text = render().textContent ?? '';

    expect(text).toContain('SOL-2026-0001');
    expect(text).toContain('En revisión');
    expect(text).toContain('Educación');
  });
});
