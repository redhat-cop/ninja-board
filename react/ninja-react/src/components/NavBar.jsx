import React, { useState } from "react";
import { NavLink } from "react-router-dom";
import {
  Nav,
  NavExpandable,
  NavItem,
  NavList,
  NavVariants,
  Button
} from "@patternfly/react-core";
import "../assets/css/nav.css";

/**
 * @author fostimus
 */
const NavBar = props => {
  const expandbleRef = React.createRef();

  // this controls CSS highlighting when selecting a link
  const [activeGroup, setActiveGroup] = useState("");
  const [activeItem, setActiveItem] = useState("");

  // Since we are using 3rd party components from PatterFly,
  // we need to use refs to manually change the expandedState
  // when an item is clicked
  const closeExpandables = () => {
    expandbleRef.current.state.expandedState = false;
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

  //TODO: Nav bar comes out of the page header when the window shrinks horizontally, and there is an obvious style change
  // TODO: styling for "Logged in as" and Logout button
  return (
    <Nav className="account-menu" onSelect={onSelect}>
      <NavList className="right-align" variant={NavVariants.horizontal}>
        <NavExpandable
          key="ninja"
          title={"Account: " + localStorage.getItem("display-name")}
          srText="Account"
          groupId="account"
          isActive={activeGroup === "account"}
          ref={expandbleRef}
        >
          <NavItem
            key="account-edit"
            groupId="account"
            itemId="account-edit"
            isActive={activeItem === "account-edit"}
            onClick={closeExpandables}
          >
            <NavLink exact to="/edit-account">
              Edit Account
            </NavLink>
          </NavItem>
          <NavItem
            key="authentication-logout"
            groupId="account"
            itemId="authentication-logout"
            isActive={activeItem === "authentication-logout"}
            onClick={logout}
          >
            Logout
          </NavItem>
        </NavExpandable>
      </NavList>
    </Nav>
  );
};

export default NavBar;
