import React, { Component } from "react";
import {
  Nav,
  NavExpandable,
  NavItem,
  NavList,
  NavVariants,
  PageHeader
} from "@patternfly/react-core";
import { NavLink } from "react-router-dom";
import { navBarLinks } from "../data/NavBarLinks";
import RedHatLogo from "../assets/media/logo.svg";

export default class NavBar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      navBarLinks: navBarLinks,
      // this controls CSS highlighting when selecting a link
      activeGroup: "grp-2",
      activeItem: "grp-2_itm-1"
    };

    this.onSelect = result => {
      this.setState({
        activeGroup: result.groupId,
        activeItem: result.itemId
      });
      console.log(this.state);
    };
  }

  render() {
    const { navBarLinks, activeGroup, activeItem } = this.state;
    // NavList uses the map function twice to populate NavExpandables and NavItems; see src/data/NavBarLinks.js for content
    //TODO: vertical alignment is off compared to the logo. verticalAlign CSS property doesn't seem to affect anything
    const nav = (
      <Nav
        style={{ marginLeft: "20px", fontSize: "20px" }}
        onSelect={this.onSelect}
        theme="dark"
      >
        <NavList variant={NavVariants.horizontal}>
          {navBarLinks.map(expandable => (
            <NavExpandable
              title={expandable.expandableName}
              srText={expandable.expandableName}
              groupId={expandable.groupId}
              isActive={activeGroup === expandable.groupId}
            >
              {expandable.links.map(link => (
                <NavItem
                  preventDefault
                  to={link.target}
                  groupId={expandable.groupId}
                  itemId={
                    expandable.groupId + "-" + link.routeName + "-" + link.id
                  }
                  isActive={
                    activeItem ===
                    expandable.groupId + "-" + link.routeName + "-" + link.id
                  }
                >
                  <NavLink exact to={link.routePath} >
                    {link.routeName}
                  </NavLink>
                </NavItem>
              ))}
            </NavExpandable>
          ))}
        </NavList>
      </Nav>
    );

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

    return (
      <PageHeader
        logo={logo}
        logoProps={logoProps}
        topNav={nav}
        style={{ backgroundColor: "rgb(21, 21, 21)" }}
      />
    );
  }
}
