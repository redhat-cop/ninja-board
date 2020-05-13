import React, { Fragment } from "react";
import { Route, Switch } from "react-router-dom";
import FormSection from "./components/UserRegistrationForm";
import AdminSection from "./components/AdminConfigurable";
import HomeSection from "./components/Home";

const AppRoutes = () => {
  return (
    <Fragment>
      <Switch>
        <Route path="/registration-form">
          <FormSection />
        </Route>
        <Route path="/config">
          <AdminSection adminPage="Config" />
        </Route>
        <Route path="/database">
          <AdminSection adminPage="Database" />
        </Route>
        <Route exact path="/">
          <HomeSection />
        </Route>
      </Switch>
    </Fragment>
  );
};

export default AppRoutes;
