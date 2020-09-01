import React, { useState, useEffect } from "react";
import { PageHeader, Page } from "@patternfly/react-core";
import { userFromToken } from "./config/ServerUrls";
import NavBar from "./components/NavBar";
import RedHatLogo from "./assets/media/logo.svg";
import AppRoutes from "./AppRoutes";

/**
 * @author fostimus
 */

const AppLayout = props => {
  // on app load, check if token exists to see if user is authenticated
  useEffect(() => {
    userFromToken(localStorage.getItem("jwt-token"))
      .then(res => {
        console.log(res);
        if (res.status === 200) {
          setLoggedIn(true);
        }
      })
      .catch(err => {
        setLoggedIn(false);
      });
  }, []);

  // setting to true by default, as there is an issue on reload when default is false. when false, routes
  // will always refresh to the login page; this is because the state changes in the useEffect of this
  // AppLayout component, and re renders. However, the route already rendered to redirect to /login and no
  // longer knows to re-render whatever page it was previously on
  const [loggedIn, setLoggedIn] = useState(true);

  const logo = (
    <img
      style={{ width: "150px", marginLeft: "20px" }}
      src={RedHatLogo}
      alt="Red Hat"
    />
  );

  // this Header construct is a PatternFly design

  // logoProps = specifies properties on the component specified in logoComponent.
  // here, we are specifying the Link component to go to our index route
  const Header = loggedIn ? (
    <PageHeader
      logo={logo}
      logoProps={{
        href: "/"
      }}
      topNav={<NavBar />}
    />
  ) : null;

  return (
    <Page mainContainerId="primary-app-container" header={Header}>
      <AppRoutes loggedIn={loggedIn} setLoggedIn={setLoggedIn} />
    </Page>
  );
};

export default AppLayout;
