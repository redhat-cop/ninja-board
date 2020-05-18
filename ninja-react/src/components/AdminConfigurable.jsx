import React, { Component, Fragment } from "react";
import { PageSection } from "@patternfly/react-core";
import "../assets/css/admin.css";

/**
 * @author fostimus
 */
export default class AdminSection extends React.Component {
  render() {
    return (
      <PageSection>
        <AdminConfigurable adminPage={this.props.adminPage} />
      </PageSection>
    )
  }
}

// component that just returns a textarea and a button to save info.
// pass in the appropriate API to set the correct data
// TODO: implement the above via API call to new quarkus app
export class AdminConfigurable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      // toggle this to switch which admin page gets updated.
      // valid values: 'Config' or 'Database'
      adminPage: this.props.adminPage
    };
  }

  render() {
    return (
      <Fragment>
        <h1>{this.state.adminPage}</h1>
        <textarea className="admin-input"></textarea>
        <div className="admin-submit">
          <button type="submit" name="save">
            Save
          </button>
        </div>
      </Fragment>
    );
  }
}
