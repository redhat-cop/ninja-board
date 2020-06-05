import React, { Fragment } from "react";
import { Route, Switch } from "react-router-dom";
import FormSection from "./components/UserRegistrationForm";
import AdminSection from "./components/AdminConfigurable";
import ScorecardSection from "./components/Scorecard";
import HomeSection from "./components/Home";

/**
 * @author fostimus
 */

//TODO: minimize places to update, use ninjaRoutes and appRoutes values directly in React Router
export const ninjaRoutes = [
  {
    id: 1,
    routeName: "Scorecards",
    routePath: "scorecards"
  },
  {
    id: 2,
    routeName: "Leaderboard",
    routePath: "leaderboard"
  },
  {
    id: 3,
    routeName: "Events",
    routePath: "events"
  },
  {
    id: 4,
    routeName: "Tasks",
    routePath: "tasks"
  },
  {
    id: 5,
    routeName: "Registration Form",
    routePath: "registration-form"
  },
  {
    id: 6,
    routeName: "Responses Spreadsheet",
    routePath: "responses-spreadsheets"
  },
  {
    id: 7,
    routeName: "Support - Trello Query",
    routePath: "support/trello-query"
  },
  {
    id: 8,
    routeName: "Support - Trello Reconciliation",
    routePath: "support/trello-reconciliation"
  }
];

export const adminRoutes = [
  {
    id: 1,
    routeName: "Config",
    routePath: "config"
  },
  {
    id: 2,
    routeName: "Database",
    routePath: "database"
  }
];

const AppRoutes = () => {
  return (
    <Fragment>
      <Switch>
        <Route
          key="user-registration-page"
          path="/registration-form"
          render={() => <FormSection />}
        />
        <Route
          key="config-page"
          path="/config"
          render={() => <AdminSection adminPage="Config" />}
        />
        <Route
          key="database-page"
          path="/database"
          render={() => <AdminSection adminPage="Database" />}
        />
        <Route
          key="scorecards-page"
          path="/scorecards"
          render={() => <ScorecardSection />}
        />
        <Route key="home-page" exact path="/" render={() => <HomeSection />} />
      </Switch>
    </Fragment>
  );
};

export default AppRoutes;
