import React from "react";
// import API from "../config/ServerUrls";
import { PageSection } from "@patternfly/react-core";

const AccountSection = props => {
  return (
    <PageSection>
      <AccountPage {...props} />
    </PageSection>
  );
};

export default AccountSection;

// maybe use redux here for stage management?
// create page/form to update account details
export const AccountPage = props => {


}
