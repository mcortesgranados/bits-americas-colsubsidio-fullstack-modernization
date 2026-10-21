const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');

/**
 * Configuración de federación del shell.
 *
 * No declara `remotes` a propósito: el shell descubre cada microfrontend en tiempo de ejecución leyendo
 * `assets/mf.manifest.json`. Así, publicar un remoto nuevo no obliga a recompilar el contenedor, que es
 * justo la propiedad que se quiere demostrar.
 */
module.exports = withModuleFederationPlugin({
  name: 'shell',
  shared: {
    ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
  },
});
