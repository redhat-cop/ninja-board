import React from 'react';
import ReactDOM from 'react-dom/client';
import '@patternfly/react-core/dist/styles/base.css';
import { AppLayout } from './components/AppLayout/AppLayout';
import './reset.css';
import Scorecards from './components/Pages/scorecards/Scorecards';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <AppLayout>
        <Scorecards></Scorecards>
    </AppLayout>
  </React.StrictMode>
);