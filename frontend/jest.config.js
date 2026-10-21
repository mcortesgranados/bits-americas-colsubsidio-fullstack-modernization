/**
 * Configuración de Jest para los tres proyectos del workspace.
 *
 * Se usa el preset de Angular, que compila las plantillas y aplica los transformadores de TypeScript
 * antes de ejecutar cada spec. Los tests viven junto al código, en `projects/`, para que un cambio de
 * contrato rompa la prueba en el mismo sitio donde se cambió.
 */
module.exports = {
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testMatch: ['<rootDir>/projects/**/*.spec.ts'],
  moduleNameMapper: {
    '^shared-core$': '<rootDir>/projects/shared-core/src/public-api.ts',
  },
  transform: {
    '^.+\\.(ts|js|mjs|html|svg)$': [
      'jest-preset-angular',
      {
        tsconfig: '<rootDir>/tsconfig.spec.json',
        stringifyContentPathRegex: '\\.(html|svg)$',
      },
    ],
  },
  transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$)'],
  modulePathIgnorePatterns: ['<rootDir>/dist/'],
  moduleFileExtensions: ['ts', 'html', 'js', 'mjs', 'json'],
  testEnvironment: 'jsdom',
};
