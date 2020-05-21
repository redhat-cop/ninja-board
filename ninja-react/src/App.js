import React from "react";
import { BrowserRouter as Router } from "react-router-dom";
import "@patternfly/react-core/dist/styles/base.css";
import AppLayout from "./AppLayout";
import AppRoutes from "./AppRoutes";

function App() {
  return (
    <Router>
      <AppLayout>
        <AppRoutes />
      </AppLayout>
    </Router>
  );
}

export default App;
