import React, { Fragment } from "react";
import { Route, Switch } from "react-router-dom";
import FormSection from "./components/UserRegistrationForm";
import AdminSection from "./components/AdminConfigurable";
import ScorecardSection from "./components/Scorecard";
import HomeSection from "./components/Home";

/**
 * @author fostimus
 */
const AppRoutes = () => {
  return (
    <Fragment>
      <Switch>
        <Route key="user-registration-page" path="/registration-form" render={() => <FormSection />} />
        <Route key="config-page" path="/config" render={() => <AdminSection adminPage="Config" />} />
        <Route key="database-page" path="/database" render={() => <AdminSection adminPage="Database" />} />
        <Route key="scorecards-page" path="/scorecards" render={() => <ScorecardSection />} />
        <Route key="home-page" exact path="/" render={() => <HomeSection />} />
      </Switch>
    </Fragment>
  );
};

export default AppRoutes;
