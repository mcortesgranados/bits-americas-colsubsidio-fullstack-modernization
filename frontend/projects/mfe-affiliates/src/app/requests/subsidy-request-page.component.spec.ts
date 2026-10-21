import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { SubsidyRequestCreated } from 'shared-core';
import { AffiliateApiService } from '../profile/affiliate-api.service';
import { SubsidyRequestPageComponent } from './subsidy-request-page.component';

const CREATED: SubsidyRequestCreated = {
  id: 7,
  requestNumber: 'SOL-2026-0007',
  status: 'SUBMITTED',
  subsidyType: 'VIVIENDA',
  requestedAmount: 4000000,
  currency: 'COP',
  submittedAt: '2026-10-21T15:40:00Z',
  requiredDocuments: ['BANK_CERTIFICATE', 'ID_DOCUMENT', 'INCOME_CERTIFICATE'],
};

/**
 * Pruebas del asistente de solicitud (UC-03).
 *
 * El servicio se sustituye por un doble, de modo que la prueba describe qué ve el afiliado y no cómo se
 * construye la petición. Se cubren los cuatro resultados que el caso de uso declara: registro exitoso con
 * radicado, monto sobre el límite (422), solicitud abierta duplicada (409) y formulario inválido que no llega
 * a la API.
 */
describe('SubsidyRequestPageComponent', () => {
  let createSubsidyRequest: jest.Mock;

  async function setup(response: Observable<SubsidyRequestCreated>): Promise<ComponentFixture<SubsidyRequestPageComponent>> {
    createSubsidyRequest = jest.fn(() => response);
    await TestBed.configureTestingModule({
      imports: [SubsidyRequestPageComponent],
      providers: [provideRouter([]), { provide: AffiliateApiService, useValue: { createSubsidyRequest } }],
    }).compileComponents();

    const fixture = TestBed.createComponent(SubsidyRequestPageComponent);
    fixture.detectChanges();
    return fixture;
  }

  function fill(
    fixture: ComponentFixture<SubsidyRequestPageComponent>,
    amount: number | null,
    purpose = 'Arriendo',
  ): void {
    fixture.componentInstance.form.setValue({
      subsidyType: 'VIVIENDA',
      requestedAmount: amount,
      purpose,
    });
  }

  it('shows the demonstration limit of the selected type', async () => {
    const fixture = await setup(of(CREATED));

    const text = fixture.nativeElement.textContent ?? '';

    expect(text).toContain('Límite de demostración para este tipo');
    expect(text).toMatch(/4[.,]500[.,]000 COP/);
  });

  it('shows the radicado and the required documents after a successful registration', async () => {
    const fixture = await setup(of(CREATED));
    fill(fixture, 4000000);

    fixture.componentInstance.submit();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent ?? '';
    expect(text).toContain('SOL-2026-0007');
    expect(text).toContain('Certificado bancario');
    expect(text).toContain('Documento de identidad');
    expect(createSubsidyRequest).toHaveBeenCalledWith({
      subsidyType: 'VIVIENDA',
      requestedAmount: 4000000,
      purpose: 'Arriendo',
    });
  });

  it('explains a 422 when the amount exceeds the demonstration limit', async () => {
    const fixture = await setup(
      throwError(() => new HttpErrorResponse({ status: 422, error: { code: 'AMOUNT_ABOVE_DEMO_LIMIT' } })),
    );
    fill(fixture, 4500001);

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent ?? '').toContain('supera el límite de demostración');
  });

  it('explains a 409 when there is already an open request of the same type', async () => {
    const fixture = await setup(
      throwError(() => new HttpErrorResponse({ status: 409, error: { code: 'DUPLICATE_OPEN_REQUEST' } })),
    );
    fill(fixture, 3000000);

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent ?? '').toContain('Ya tienes una solicitud abierta de este tipo');
  });

  it('does not call the API when the amount is missing', async () => {
    const fixture = await setup(of(CREATED));
    fill(fixture, null);

    fixture.componentInstance.submit();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent ?? '';
    expect(createSubsidyRequest).not.toHaveBeenCalled();
    expect(text).toContain('El monto es obligatorio.');
  });
});
