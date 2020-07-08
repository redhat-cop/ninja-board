import React, { useState } from "react";
import { PageSection } from "@patternfly/react-core";
import ConfirmationModal from "./ConfirmationModal";

/**
 * @author fostimus
 */
const LoginSection = props => {
  return (
    <PageSection>
      <LoginPage />
    </PageSection>
  );
}

export default LoginSection;

export const LoginPage = props => {
  const [showModal, setShowModal] = useState(true);

  const handleModalToggle = () => {
    setShowModal(!showModal);
  }
  return (
    <ConfirmationModal
      showModal={true}
      handleModalToggle={handleModalToggle}
      modalTitle="Login Modal"
      modalText="Please log in here"
    />
  );
};
