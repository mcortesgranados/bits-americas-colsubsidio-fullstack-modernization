import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL, StatusChangedEvent, readDemoToken } from 'shared-core';

const RECONNECT_MS = 3000;

/**
 * Cliente SSE del estado de una solicitud (UC-05).
 *
 * No se usa `EventSource` porque no permite enviar la cabecera `Authorization`, y el endpoint exige el token:
 * pasarlo por la URL lo dejaría en historiales y logs. En su lugar se usa `fetch` con `ReadableStream`, que sí
 * admite cabeceras. El parser implementa el subconjunto del protocolo que el backend emite (`id`, `data` y
 * comentarios de latido) y descarta los latidos.
 *
 * Ante un corte se reconecta a los tres segundos y reenvía `Last-Event-ID`, así que el backend repite solo lo
 * que el navegador se perdió (flujo alterno A2). El observable limpia el temporizador y aborta la petición al
 * darse de baja, de modo que cerrar la pestaña o navegar fuera no deja conexiones abiertas.
 */
@Injectable({ providedIn: 'root' })
export class StatusStreamService {

  private readonly apiBaseUrl = inject(API_BASE_URL);

  /**
   * Abre el stream de una solicitud y emite cada cambio de estado.
   *
   * @param requestId solicitud a seguir
   * @returns observable que se reconecta mientras siga suscrito
   */
  stream(requestId: number): Observable<StatusChangedEvent> {
    return new Observable<StatusChangedEvent>((subscriber) => {
      const controller = new AbortController();
      let lastEventId: string | null = null;
      let reconnectHandle: ReturnType<typeof setTimeout> | null = null;
      let closed = false;

      const scheduleReconnect = (): void => {
        if (!closed) {
          reconnectHandle = setTimeout(() => void connect(), RECONNECT_MS);
        }
      };

      const connect = async (): Promise<void> => {
        const token = readDemoToken();
        const headers: Record<string, string> = { Accept: 'text/event-stream' };
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }
        if (lastEventId) {
          headers['Last-Event-ID'] = lastEventId;
        }

        try {
          const response = await fetch(`${this.apiBaseUrl}/api/v1/subsidy-requests/${requestId}/events`, {
            headers,
            signal: controller.signal,
          });
          if (!response.ok || !response.body) {
            scheduleReconnect();
            return;
          }

          const reader = response.body.getReader();
          const decoder = new TextDecoder();
          let buffer = '';

          while (!closed) {
            const { value, done } = await reader.read();
            if (done) {
              break;
            }
            buffer += decoder.decode(value, { stream: true });
            const frames = buffer.split(/\r?\n\r?\n/);
            buffer = frames.pop() ?? '';
            for (const frame of frames) {
              const parsed = this.parseFrame(frame);
              if (parsed.id) {
                lastEventId = parsed.id;
              }
              if (parsed.data) {
                try {
                  subscriber.next(JSON.parse(parsed.data) as StatusChangedEvent);
                } catch {
                  // Un frame ilegible no debe romper el stream; el siguiente evento lo reemplaza.
                }
              }
            }
          }
        } catch {
          // Error de red o aborto: se reintenta abajo salvo que la suscripción esté cerrada.
        }
        scheduleReconnect();
      };

      void connect();

      return () => {
        closed = true;
        controller.abort();
        if (reconnectHandle) {
          clearTimeout(reconnectHandle);
        }
      };
    });
  }

  /**
   * Extrae el identificador y el contenido de un frame SSE.
   *
   * @param frame bloque separado por una línea en blanco
   * @returns el id y el data acumulado, ambos opcionales
   */
  private parseFrame(frame: string): { id: string | null; data: string | null } {
    let id: string | null = null;
    const data: string[] = [];
    for (const line of frame.split('\n')) {
      if (line.startsWith('id:')) {
        id = line.slice(3).trim();
      } else if (line.startsWith('data:')) {
        data.push(line.slice(5).trimStart());
      }
    }
    return { id, data: data.length > 0 ? data.join('\n') : null };
  }
}
