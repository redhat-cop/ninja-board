import React, { Component, Fragment } from "react";
import NavBar from "./NavBar";
import "../assets/css/admin.css";

// component that just returns a textarea and a button to save info.
// pass in the appropriate API to set the correct data
// TODO: implement the above via API call to new quarkus app
class AdminConfigurable extends Component {
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
        <NavBar />
        <h1>{this.state.adminPage}</h1>
        <textarea class="admin-input"></textarea>
        <div class="admin-submit">
          <button type="submit" name="save">
            Save
          </button>
        </div>
      </Fragment>
    );
  }
}

export default AdminConfigurable;
