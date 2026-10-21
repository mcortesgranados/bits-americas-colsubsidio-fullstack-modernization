import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';
import { Beneficiary } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';
import { BeneficiariesPageComponent } from './beneficiaries-page.component';

const BENEFICIARIES: Beneficiary[] = [
  {
    id: 1,
    fullName: 'Julián Rojas Rojas',
    relationship: 'HIJO',
    documentNumber: '******4050',
    birthDate: '2018-09-30',
    status: 'ACTIVE',
  },
  {
    id: 2,
    fullName: 'Sofía Rojas Rojas',
    relationship: 'HIJO',
    documentNumber: '******3040',
    birthDate: '2015-02-18',
    status: 'ACTIVE',
  },
];

/**
 * Pruebas de la pantalla de beneficiarios (UC-02).
 *
 * El servicio se sustituye por un doble, de modo que la prueba describe qué se pinta y no cómo se
 * obtiene. Se cubren los tres resultados que el caso de uso declara: lista con documentos enmascarados,
 * lista vacía (flujo alterno A1) y acceso denegado (flujo alterno A2), que debe mostrar un mensaje propio
 * en lugar del genérico de red.
 */
describe('BeneficiariesPageComponent', () => {
  async function setup(response: Observable<Beneficiary[]>): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [BeneficiariesPageComponent],
      providers: [{ provide: AffiliateApiService, useValue: { getBeneficiaries: () => response } }],
    }).compileComponents();
  }

  function render(): HTMLElement {
    const fixture = TestBed.createComponent(BeneficiariesPageComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('renders each beneficiary with the relationship translated and the masked document', async () => {
    await setup(of(BENEFICIARIES));

    const text = render().textContent ?? '';

    expect(text).toContain('Julián Rojas Rojas');
    expect(text).toContain('Sofía Rojas Rojas');
    expect(text).toContain('Hijo/a');
    expect(text).toContain('******4050');
    expect(text).not.toContain('1025304050');
  });

  it('shows the empty state when the affiliate has no beneficiaries', async () => {
    await setup(of([]));

    const text = render().textContent ?? '';

    expect(text).toContain('No tienes beneficiarios registrados');
  });

  it('shows a permission message when the API answers 403', async () => {
    await setup(throwError(() => new HttpErrorResponse({ status: 403 })));

    const element = render();

    expect(element.textContent ?? '').toContain('No tienes permiso para consultar estos beneficiarios.');
    expect(element.querySelector('[role="status"]')?.textContent).toContain('No tienes permiso');
  });
});
