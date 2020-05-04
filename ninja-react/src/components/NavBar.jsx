import React, { Component } from "react";
import { NavLink } from 'react-router-dom';
import {
  Nav,
  NavExpandable,
  NavItem,
  NavItemSeparator,
  NavList,
  NavGroup,
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
    };
  }

  render() {
    const { navBarLinks, activeGroup, activeItem } = this.state;
    return (
      <Nav onSelect={this.onSelect} theme="dark">
        <NavList>
          {navBarLinks.map(expandable => (
            <NavExpandable
              title={expandable.expandableName}
              srText="not sure what this is yet"
              groupId={expandable.groupId}
              isActive={activeGroup === expandable.groupId}
              isExpanded
            >
              {expandable.links.map(link => (
                <NavItem
                  preventDefault
                  to={link.target}
                  groupId="ninja"
                  itemId={"ninja-" + link.id}
                  isActive={activeItem === "ninja-" + link.id}
                >
                  {link.linkName}
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
