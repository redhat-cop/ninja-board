import React, { Component } from "react";
import { PageHeader, PageSection, Page } from "@patternfly/react-core";
import NavBar from "./components/NavBar";
import UserRegistrationForm from "./components/UserRegistrationForm";
import RedHatLogo from "./assets/media/logo.svg";

class AppLayout extends Component {
  render() {
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

    const Header = (
      <PageHeader
        logo={logo}
        logoProps={logoProps}
        topNav={<NavBar />}
        style={{ backgroundColor: "rgb(21, 21, 21)" }}
      />
    );

    return (
      <Page mainContainerId="primary-app-container" header={Header}>
        <PageSection>
          <UserRegistrationForm />
        </PageSection>
      </Page>
    );
  }
}

export default AppLayout;
