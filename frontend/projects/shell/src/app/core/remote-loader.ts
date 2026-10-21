import { loadRemoteModule } from '@angular-architects/module-federation';
import { Routes } from '@angular/router';
import { CONTRACT_VERSION, MfManifest, RemoteDefinition } from 'shared-core';
import { RemoteUnavailableComponent } from '../pages/remote-unavailable/remote-unavailable.component';

/**
 * Carga las rutas de un microfrontend resolviéndolo en tiempo de ejecución.
 *
 * El shell no conoce las URLs de sus remotos en compilación: las lee de `assets/mf.manifest.json` con
 * `cache: 'no-store'`, de modo que publicar un remoto nuevo o cambiar su host no obliga a reconstruir el
 * contenedor. Antes de montar nada compara la versión mayor del contrato declarada por el remoto con la
 * que habla el shell; una diferencia produce un error explícito en vez de una pantalla rota.
 *
 * Si el remoto no responde, la ruta hija muestra una pantalla de indisponibilidad con el motivo. Es
 * preferible a dejar la navegación a medias: el usuario ve que ese portal no está disponible y el resto
 * del shell sigue funcionando.
 *
 * @param name clave del remoto en el manifiesto, por ejemplo `mfeAffiliates`
 * @returns las rutas que el router del shell monta bajo la ruta contenedora
 */
export async function loadRemoteRoutes(name: string): Promise<Routes> {
  try {
    const manifest = await fetch('assets/mf.manifest.json', { cache: 'no-store' }).then(
      (response) => response.json() as Promise<MfManifest>,
    );

    const remote: RemoteDefinition | undefined = manifest[name];
    if (!remote) {
      throw new Error(`El remoto "${name}" no está declarado en assets/mf.manifest.json`);
    }

    const remoteMajor = remote.contractVersion.split('.')[0];
    const shellMajor = CONTRACT_VERSION.split('.')[0];
    if (remoteMajor !== shellMajor) {
      throw new Error(
        `Contrato incompatible en "${name}": el shell habla ${CONTRACT_VERSION} y el remoto declara ${remote.contractVersion}`,
      );
    }

    const exposed = await loadRemoteModule({
      type: 'module',
      remoteEntry: remote.remoteEntry,
      exposedModule: remote.exposedModule,
    });

    return exposed.routes as Routes;
  } catch (error) {
    const reason = error instanceof Error ? error.message : 'Error desconocido al cargar el microfrontend';
    console.error(`[shell] ${reason}`);
    return [
      {
        path: '**',
        component: RemoteUnavailableComponent,
        data: { reason },
      },
    ];
  }
}

