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
      console.log(this.state);
    };
  }

  //TODO: need to implement a handler to both toggle and style a selected expandable. default behavior is just to toggle the expandable, but still white background
  onExpandHandler = (groupId) => {
    this.setState({
      activeGroup: groupId
    })
  }

  render() {
    const { navBarLinks, activeGroup, activeItem } = this.state;
    return (
      <Nav onSelect={this.onSelect} theme="dark">
        <NavList>
          {navBarLinks.map(expandable => (
            <NavExpandable
              title={expandable.expandableName}
              srText={expandable.expandableName}
              groupId={expandable.groupId}
              isActive={activeGroup === expandable.groupId}
              // onExpand={()=>this.onExpandHandler(expandable.groupId)}
            >
              {expandable.links.map(link => (
                <NavItem
                  preventDefault
                  to={link.target}
                  groupId={expandable.groupId}
                  itemId={expandable.groupId + '-' + link.linkName + '-' + link.id}
                  isActive={activeItem === expandable.groupId + '-' + link.linkName + '-' + link.id}
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
