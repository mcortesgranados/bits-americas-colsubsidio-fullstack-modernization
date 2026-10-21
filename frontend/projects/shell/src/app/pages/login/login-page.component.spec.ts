import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { LoginPageComponent } from './login-page.component';

/**
 * Pruebas de la pantalla de acceso.
 *
 * Cubren lo que el evaluador necesita para entrar sin leer el repositorio: que las cuentas de demostración
 * se listen junto con su contraseña y que el botón "Usar" rellene el formulario, sin enviarlo.
 */
describe('LoginPageComponent', () => {
  async function setup() {
    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: { login: jest.fn() } }],
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('lists the demo accounts and the shared password', async () => {
    const fixture = await setup();

    const text = fixture.nativeElement.textContent ?? '';

    expect(text).toContain('Usuarios de prueba');
    expect(text).toContain('afiliado.demo');
    expect(text).toContain('empresa.demo');
    expect(text).toContain('backoffice.demo');
    expect(text).toContain('auditor.demo');
    expect(text).toContain('Demo123!');
  });

  it('fills the form when a demo account is chosen', async () => {
    const fixture = await setup();

    const button = fixture.nativeElement.querySelector('.demo__use') as HTMLButtonElement;
    button.click();
    fixture.detectChanges();

    expect(fixture.componentInstance.form.getRawValue()).toEqual({
      username: 'afiliado.demo',
      password: 'Demo123!',
    });
  });
});
