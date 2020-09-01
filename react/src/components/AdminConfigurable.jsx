import React, { Fragment } from "react";
import { PageSection } from "@patternfly/react-core";
import "../assets/css/admin.css";

/**
 * @author fostimus
 */
const AdminSection = props => {
  return (
    <PageSection>
      <AdminConfigurable adminPage={props.adminPage} />
    </PageSection>
  );
};

export default AdminSection;

// component that just returns a textarea and a button to save info.
// pass in the appropriate API to set the correct data
// TODO: implement the above via API call to new quarkus app
export const AdminConfigurable = props => {
  // if (props.adminPage !== 'Config' && props.adminPage !== 'Database') {
  //   throw new Error("Invalid value; must be 'Config' or 'Database'");
  // }

  return (
    <Fragment>
      <h1>{props.adminPage}</h1>
      <textarea className="admin-input"></textarea>
      <div className="admin-submit">
        <button type="submit" name="save">
          Save
        </button>
      </div>
    </Fragment>
  );
};
