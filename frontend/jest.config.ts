/**
 * Jest configuration for the React frontend.
 *
 * Uses ts-jest preset with jsdom environment for DOM testing.
 * Maps CSS/asset imports to identity-obj-proxy so component imports
 * don't fail during tests.
 */

import type { Config } from 'jest';

const config: Config = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  roots: ['<rootDir>/src'],
  testMatch: [
    '**/__tests__/**/*.{ts,tsx}',
    '**/*.{spec,test}.{ts,tsx}',
  ],
  moduleNameMapper: {
    // Map CSS / SCSS / LESS imports to identity-obj-proxy
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    // Map static asset imports (images, fonts, etc.) to a stub
    '\\.(jpg|jpeg|png|gif|svg|webp|woff|woff2|eot|ttf|otf)$':
      '<rootDir>/src/__mocks__/fileMock.ts',
  },
  setupFilesAfterEnv: ['@testing-library/jest-dom'],
  transform: {
    '^.+\\.tsx?$': [
      'ts-jest',
      {
        tsconfig: 'tsconfig.json',
        // Allow JSX in test files
        jsx: 'react-jsx',
      },
    ],
  },
  // Ignore Vite-specific files
  transformIgnorePatterns: ['/node_modules/'],
};

export default config;
