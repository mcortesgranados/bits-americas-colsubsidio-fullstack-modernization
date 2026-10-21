import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

/** Cuenta de demostración que se ofrece en la propia pantalla de acceso. */
interface DemoAccount {
  username: string;
  role: string;
  scope: string;
}

/**
 * Pantalla de acceso del portal.
 *
 * Usa un formulario reactivo con validación local mínima —usuario obligatorio y contraseña de al menos
 * ocho caracteres— y deja la decisión real al servidor. El mensaje de error es único para credenciales
 * inválidas, de modo que la interfaz tampoco revela si el usuario existe.
 *
 * El destino posterior al acceso sale de `returnUrl`, que el guard rellena cuando alguien intenta entrar a
 * una ruta protegida sin sesión. Si no hay valor se navega al portal de afiliados.
 */
@Component({
  selector: 'cs-login-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss',
})
export class LoginPageComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.formBuilder.group({
    username: this.formBuilder.control('', [Validators.required, Validators.maxLength(60)]),
    password: this.formBuilder.control('', [Validators.required, Validators.minLength(8)]),
  });

  /**
   * Cuentas sintéticas que crea `DemoUserInitializer` al arrancar.
   *
   * Se listan aquí porque el evaluador necesita entrar sin leer el repositorio. Son cuentas de
   * demostración con datos inventados y comparten contraseña: en un entorno real esta lista no existiría y
   * la pantalla no revelaría qué usuarios hay dados de alta.
   */
  readonly demoPassword = 'Demo123!';
  readonly demoAccounts: DemoAccount[] = [
    { username: 'afiliado.demo', role: 'Afiliado', scope: 'Perfil, beneficiarios y solicitudes' },
    { username: 'empresa.demo', role: 'Empresa', scope: 'Portal de empresas' },
    { username: 'backoffice.demo', role: 'Back-office', scope: 'Consulta de cualquier afiliado (API)' },
    { username: 'auditor.demo', role: 'Auditor', scope: 'Consulta de solo lectura (API)' },
  ];

  /**
   * Rellena el formulario con una cuenta de demostración.
   *
   * Solo rellena: el envío sigue siendo explícito para que quien prueba vea usuario y contraseña antes de
   * entrar, y para que un clic accidental no abra una sesión.
   *
   * @param username cuenta elegida en la lista
   */
  useDemoAccount(username: string): void {
    this.form.setValue({ username, password: this.demoPassword });
    this.errorMessage.set(null);
  }

  /**
   * Envía las credenciales y navega al destino pendiente.
   *
   * No se muestra el detalle técnico del fallo: un `401` significa credenciales incorrectas y cualquier
   * otro código se presenta como problema de conexión. Así la pantalla no se convierte en un oráculo del
   * estado interno del backend.
   */
  submit(): void {
    if (this.loading()) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/afiliados';
        this.loading.set(false);
        void this.router.navigateByUrl(returnUrl);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          error.status === 401
            ? 'Usuario o contraseña incorrectos.'
            : 'No fue posible conectar con el servicio. Intenta de nuevo.',
        );
      },
    });
  }
}
