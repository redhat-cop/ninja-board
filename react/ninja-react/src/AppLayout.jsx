import React from "react";
import { PageHeader, Page } from "@patternfly/react-core";
import NavBar from "./components/NavBar";
import RedHatLogo from "./assets/media/logo.svg";

/**
 * @author fostimus
 */

const AppLayout = ({ children }) => {
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
  const Header = (
    <PageHeader
      logo={logo}
      logoProps={logoProps}
      topNav={<NavBar />}
    />
  );

  return (
    <Page mainContainerId="primary-app-container" header={Header}>
      {children}
    </Page>
  );
};

export default AppLayout;
