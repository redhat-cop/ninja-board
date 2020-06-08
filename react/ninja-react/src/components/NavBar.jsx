import React, { Component } from "react";
import { NavLink, useRouteMatch } from "react-router-dom";
import {
  Nav,
  NavExpandable,
  NavItem,
  NavList,
  NavVariants
} from "@patternfly/react-core";
import { ninjaRoutes, adminRoutes } from "../AppRoutes";

/**
 * @author fostimus
 */
function NavBar(props) {
  constructor(props) {
    super(props);
    this.ninjaExpandbleRef = React.createRef();
    this.adminExpandbleRef = React.createRef();
    this.state = {
      // this controls CSS highlighting when selecting a link
      activeGroup: "",
      activeItem: ""
    };

    // Since we are using 3rd party components from PatterFly,
    // we need to use refs to manually change the expandedState
    // when an item is clicked
    this.closeExpandables = () => {
      this.ninjaExpandbleRef.current.state.expandedState = false;
      this.adminExpandbleRef.current.state.expandedState = false;
    };

    this.onSelect = result => {
      this.setState({
        activeGroup: result.groupId,
        activeItem: result.itemId
      });
    };
  }

    const { activeGroup, activeItem } = this.state;
    const { url } = useRouteMatch();


    // NavList uses the map function twice to populate NavExpandables and NavItems; see src/data/NavBarLinks.js for content
    //TODO: vertical alignment is off compared to the logo. verticalAlign CSS property doesn't seem to affect anything
    //TODO: Nav bar comes out of the page header when the window shrinks horizontally, and there is an obvious style change
    return (
      <Nav
        style={{ marginLeft: "20px", fontSize: "20px" }}
        onSelect={this.onSelect}
      >
        <NavList variant={NavVariants.horizontal}>
          <NavExpandable
            key="ninja"
            title="Ninja Program"
            srText="Ninja Program"
            groupId="ninja"
            isActive={activeGroup === "ninja"}
            ref={this.ninjaExpandbleRef}
          >
            {ninjaRoutes.map(link => (
              <NavItem
                key={"ninja-" + link.routeName + "-" + link.id}
                groupId="ninja"
                itemId={"ninja-" + link.routeName + "-" + link.id}
                isActive={
                  activeItem === "ninja-" + link.routeName + "-" + link.id
                }
                onClick={this.closeExpandables}
              >
                <NavLink exact to={`${url}/${link.routePath}`}>
                  {link.routeName}
                </NavLink>
              </NavItem>
            ))}
          </NavExpandable>
          <NavExpandable
            key="admin"
            title="Admin"
            srText="Admin"
            groupId="admin"
            isActive={activeGroup === "admin"}
            ref={this.adminExpandbleRef}
          >
            {adminRoutes.map(link => (
              <NavItem
                key={"admin-" + link.routeName + "-" + link.id}
                groupId="admin"
                itemId={"admin-" + link.routeName + "-" + link.id}
                isActive={
                  activeItem === "admin-" + link.routeName + "-" + link.id
                }
                onClick={this.closeExpandables}
              >
                <NavLink exact to={`${url}/${link.routePath}`}>
                  {link.routeName}
                </NavLink>
              </NavItem>
            ))}
          </NavExpandable>
        </NavList>
      </Nav>
    );

}

export default NavBar;
