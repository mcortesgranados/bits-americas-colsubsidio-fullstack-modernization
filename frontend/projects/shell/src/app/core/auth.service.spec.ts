import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

/**
 * Pruebas del estado de sesión.
 *
 * Se sustituye la red por el controlador de pruebas de Angular, así que las pruebas verifican el contrato
 * HTTP —ruta, método y cuerpo— y el efecto sobre el almacenamiento, sin backend.
 */
describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('starts without a session', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('posts the credentials and stores the token', () => {
    service.login({ username: 'afiliado.demo', password: 'Demo123!' }).subscribe();

    const request = http.expectOne('/api/v1/auth/demo-token');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.username).toBe('afiliado.demo');

    request.flush({
      accessToken: 'signed-token',
      tokenType: 'Bearer',
      expiresInSeconds: 7200,
      role: 'ROLE_AFFILIATE',
      affiliateId: 1,
      companyId: null,
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.token()).toBe('signed-token');
    expect(sessionStorage.getItem('cs.demo.token')).toBe('signed-token');
  });

  it('clears the session on logout', () => {
    sessionStorage.setItem('cs.demo.token', null as unknown as string);
    service.login({ username: 'afiliado.demo', password: 'Demo123!' }).subscribe();
    http
      .expectOne('/api/v1/auth/demo-token')
      .flush({ accessToken: 'signed-token', tokenType: 'Bearer', expiresInSeconds: 7200, role: 'ROLE_AFFILIATE', affiliateId: 1, companyId: null });

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('cs.demo.token')).toBeNull();
  });
});
