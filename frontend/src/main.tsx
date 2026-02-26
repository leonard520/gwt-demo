/**
 * Application entry point.
 *
 * Replaces the GWT onModuleLoad in Application.java:
 *   - Creates the root React tree
 *   - Mounts App component into the DOM
 */

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
