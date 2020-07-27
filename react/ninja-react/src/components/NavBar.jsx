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

  const style = {
    color: "blue"
  }

  // NavList uses the map function twice to populate NavExpandables and NavItems; see src/data/NavBarLinks.js for content
  //TODO: vertical alignment is off compared to the logo. verticalAlign CSS property doesn't seem to affect anything
  //TODO: Nav bar comes out of the page header when the window shrinks horizontally, and there is an obvious style change
  // TODO: styling for "Logged in as" and Logout button
  return (
    <Nav style={{ marginLeft: "20px", fontSize: "20px" }} onSelect={onSelect}>
      <NavList variant={NavVariants.horizontal}>
        <NavExpandable
          key="ninja"
          title="Account"
          srText="Account"
          groupId="account"
          isActive={activeGroup === "account"}
          ref={expandbleRef}
          style={style}
        >
          <NavItem
            key="authentication-status"
            groupId="account"
            itemId="authentication-status"
            isActive={activeItem === "authentication-status"}
            onClick={closeExpandables}
          >
            Logged in as: {localStorage.getItem("display-name")}
            <Button onClick={logout} type="submit" variant="tertiary">
              Logout
            </Button>
          </NavItem>
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
        </NavExpandable>
      </NavList>
    </Nav>
  );
};

export default NavBar;
