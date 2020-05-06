import React from "react";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";

import NavBar from "./components/NavBar";
import AdminConfigurable from "./components/AdminConfigurable";

function App() {
  return (
    <Router>
      <NavBar>
        <Switch>
          <Route path="/config">
            <AdminConfigurable adminPage="Config" />
          </Route>
          <Route path="/database">
            <AdminConfigurable adminPage="Database" />
          </Route>
        </Switch>
      </NavBar>
    </Router>
  );
}

export default App;
