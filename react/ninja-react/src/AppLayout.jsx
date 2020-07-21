import React, { useState } from "react";
import { PageHeader, Page } from "@patternfly/react-core";
import NavBar from "./components/NavBar";
import RedHatLogo from "./assets/media/logo.svg";
import AppRoutes from "./AppRoutes";

/**
 * @author fostimus
 */

const AppLayout = props => {
  const [loggedIn, setLoggedIn] = useState(false);

  const logoProps = {
    href: "https://redhat.com",
    onClick: () => console.log("clicked logo"),
    target: "_blank"
  };

  const logo = (
    <img
      style={{ width: "150px", marginLeft: "20px" }}
      src={RedHatLogo}
      alt="Red Hat"
    />
  );

  // this Header construct is a PatternFly design
  const Header = loggedIn ? (
    <PageHeader logo={logo} logoProps={logoProps} topNav={<NavBar />} />
  ) : null;

  return (
    <Page mainContainerId="primary-app-container" header={Header}>
      <AppRoutes loggedIn={loggedIn} setLoggedIn={setLoggedIn} />
    </Page>
  );
};

export default AppLayout;
