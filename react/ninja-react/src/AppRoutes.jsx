import React, { Fragment } from "react";
import { Route, Switch, Redirect } from "react-router-dom";
import { PageSection } from "@patternfly/react-core";
import FormSection from "./components/UserRegistrationForm";
import AdminSection from "./components/AdminConfigurable";
import ScorecardSection from "./components/Scorecard";
import LoginSection from "./components/NinjaLogin";
import AccountSection from "./components/AccountPage";

/**
 * @author fostimus
 */

// edit/add routes here
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
    routeName: "Responses Spreadsheet",
    routePath: "/responses-spreadsheets"
  },
  {
    id: 5,
    routeName: "Support - Trello Query",
    routePath: "/support/trello-query"
  },
  {
    id: 6,
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

export const accountRoutes = [
  {
    id: 1,
    routeName: "Registration Form",
    routePath: "/registration-form",
    component: FormSection
  },
  {
    id: 2,
    routeName: "Edit Account",
    routePath: "/edit-account",
    component: AccountSection
  },
]

const AppRoutes = props => {
  // TODO: add in a third case.
  // 1. not logged in --> routed to login page
  // 2. logged in, but not registered --> edit account/registration
  // 3. logged in and registered --> scorecards
  const firstComponent = properties =>
    !props.loggedIn ? (
      <LoginSection
        {...properties}
        loggedIn={props.loggedIn}
        setLoggedIn={props.setLoggedIn}
      />
    ) : (
      // will be profile page when it's made
      <Redirect to="/scorecards" />
    );

  return (
    <Fragment>
      <Switch>
        {/*TODO: home page shouldn't be login; landing page should, but home should be ______*/}
        <Route
          key="home"
          exact
          path="/"
          render={properties => firstComponent(properties)}
        />
        <Route
          key="login"
          path="/login"
          render={properties => firstComponent(properties)}
        />

        {ninjaRoutes.map(route => (
          <Route key={route.routeName} path={route.routePath}>
            {props.loggedIn ? route.component : <Redirect to="/login" />}
          </Route>
        ))}

        {adminRoutes.map(route => (
          <Route key={route.routeName} path={route.routePath}>
            {props.loggedIn ? (
              //TODO: use route.component and pass in route.adminPage as prop
              <AdminSection adminPage={route.adminPage} />
            ) : (
              <Redirect to="/login" />
            )}
          </Route>
        ))}

        {accountRoutes.map(route => (
          <Route key={route.routeName} path={route.routePath}>
            {props.loggedIn ? route.component : <Redirect to="/login" />}
          </Route>
        ))}

        <Route
          key="not-found"
          path="*"
          render={() => (
            <PageSection>
              <div>Not Found</div>
            </PageSection>
          )}
        />
      </Switch>
    </Fragment>
  );
};

export default AppRoutes;
