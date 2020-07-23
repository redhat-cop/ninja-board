import React, { useState, Fragment } from "react";
import { NavLink } from "react-router-dom";
import {
  Nav,
  NavExpandable,
  NavItem,
  NavList,
  NavVariants,
  Button
} from "@patternfly/react-core";
import { ninjaRoutes, adminRoutes } from "../AppRoutes";

/**
 * @author fostimus
 */
const NavBar = props => {
  const ninjaExpandbleRef = React.createRef();
  const adminExpandbleRef = React.createRef();

  // this controls CSS highlighting when selecting a link
  const [activeGroup, setActiveGroup] = useState("");
  const [activeItem, setActiveItem] = useState("");

  // Since we are using 3rd party components from PatterFly,
  // we need to use refs to manually change the expandedState
  // when an item is clicked
  const closeExpandables = () => {
    ninjaExpandbleRef.current.state.expandedState = false;
    adminExpandbleRef.current.state.expandedState = false;
  };

  const onSelect = result => {
    setActiveGroup(result.groupId);
    setActiveItem(result.itemId);
  };

  const logout = () => {
    localStorage.removeItem("jwt-token");
    localStorage.removeItem("display-name");
    window.location.reload();
  };

  // NavList uses the map function twice to populate NavExpandables and NavItems; see src/data/NavBarLinks.js for content
  //TODO: vertical alignment is off compared to the logo. verticalAlign CSS property doesn't seem to affect anything
  //TODO: Nav bar comes out of the page header when the window shrinks horizontally, and there is an obvious style change
  // TODO: styling for "Logged in as" and Logout button
  return (
    <Nav style={{ marginLeft: "20px", fontSize: "20px" }} onSelect={onSelect}>
      <NavList variant={NavVariants.horizontal}>
        <NavExpandable
          key="ninja"
          title="Ninja Program"
          srText="Ninja Program"
          groupId="ninja"
          isActive={activeGroup === "ninja"}
          ref={ninjaExpandbleRef}
        >
          {ninjaRoutes.map(link => (
            <NavItem
              key={"ninja-" + link.routeName + "-" + link.id}
              groupId="ninja"
              itemId={"ninja-" + link.routeName + "-" + link.id}
              isActive={
                activeItem === "ninja-" + link.routeName + "-" + link.id
              }
              onClick={closeExpandables}
            >
              <NavLink exact to={link.routePath}>
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
          ref={adminExpandbleRef}
        >
          {adminRoutes.map(link => (
            <NavItem
              key={"admin-" + link.routeName + "-" + link.id}
              groupId="admin"
              itemId={"admin-" + link.routeName + "-" + link.id}
              isActive={
                activeItem === "admin-" + link.routeName + "-" + link.id
              }
              onClick={closeExpandables}
            >
              <NavLink exact to={link.routePath}>
                {link.routeName}
              </NavLink>
            </NavItem>
          ))}
        </NavExpandable>
      </NavList>
      <Fragment>
        Logged in as: {localStorage.getItem("display-name")}
        <Button onClick={logout} type="submit" variant="tertiary">
          Logout
        </Button>
      </Fragment>
    </Nav>
  );
};

export default NavBar;
