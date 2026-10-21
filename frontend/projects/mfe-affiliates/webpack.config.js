const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');

/**
 * Configuración de federación del remoto de afiliados.
 *
 * Expone las rutas del portal bajo `./Routes`. El shell no importa componentes: pide ese módulo y monta
 * el router del remoto, de modo que el contrato entre ambos es una lista de rutas y no un componente.
 */
module.exports = withModuleFederationPlugin({
  name: 'mfeAffiliates',
  filename: 'remoteEntry.js',
  exposes: {
    './Routes': './projects/mfe-affiliates/src/app/remote-entry/entry.routes.ts',
  },
  shared: {
    ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
  },
});
