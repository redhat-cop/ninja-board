import React, { Fragment } from "react";
import { Route, Switch } from "react-router-dom";
import FormSection from "./components/UserRegistrationForm";
import AdminSection from "./components/AdminConfigurable";
import ScorecardSection from "./components/Scorecard";
import LoginSection from "./components/NinjaLogin";

/**
 * @author fostimus
 */

export const ninjaRoutes = [
  {
    id: 1,
    routeName: "Scorecards",
    routePath: "/scorecards",
    component: ScorecardSection
  },
  {
    id: 2,
    routeName: "Leaderboard",
    routePath: "/leaderboard"
  },
  {
    id: 3,
    routeName: "Events",
    routePath: "/events"
  },
  {
    id: 4,
    routeName: "Tasks",
    routePath: "/tasks"
  },
  {
    id: 5,
    routeName: "Registration Form",
    routePath: "/registration-form",
    component: FormSection
  },
  {
    id: 6,
    routeName: "Responses Spreadsheet",
    routePath: "/responses-spreadsheets"
  },
  {
    id: 7,
    routeName: "Support - Trello Query",
    routePath: "/support/trello-query"
  },
  {
    id: 8,
    routeName: "Support - Trello Reconciliation",
    routePath: "/support/trello-reconciliation"
  }
];

//TODO: leverage component field in <Switch> component
export const adminRoutes = [
  {
    id: 1,
    routeName: "Config",
    routePath: "/config",
    component: AdminSection,
    adminPage: "Config"
  },
  {
    id: 2,
    routeName: "Database",
    routePath: "/database",
    component: AdminSection,
    adminPage: "Database"
  }
];

const AppRoutes = () => {
  return (
    <Fragment>
      <Switch>
        {ninjaRoutes.map(route => (
          <Route
            key={route.routeName}
            path={route.routePath}
            component={route.component}
          />
        ))}

        {adminRoutes.map(route => (
          <Route
            key={route.routeName}
            path={route.routePath}
            render={props => (
              <AdminSection {...props} adminPage={route.adminPage} />
            )}
          />
        ))}

        <Route key="login" exact path="/" render={() => <LoginSection />} />
      </Switch>
    </Fragment>
  );
};

export default AppRoutes;
