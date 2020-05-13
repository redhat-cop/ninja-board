import React, { Component } from "react";
import { NavLink } from "react-router-dom";
import {
  Nav,
  NavExpandable,
  NavItem,
  NavList,
  NavVariants
} from "@patternfly/react-core";
import { navBarLinks } from "../data/NavBarLinks";

class NavBar extends Component {
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
    //TODO: Nav bar comes out of the page header when the window shrinks horizontally, and there is an obvious style change
    return (
      <Nav
        style={{ marginLeft: "20px", fontSize: "20px" }}
        onSelect={this.onSelect}
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
  }
}

export default NavBar;
