import React from "react";
import { Modal, Button } from "@patternfly/react-core";
import "../assets/css/modal.css";

/**
 * @author fostimus
 */
const ConfirmationModal = props => {
  return (
    <React.Fragment>
      <Modal
        className="modal"
        variant="small"
        title={props.modalTitle}
        isOpen={props.showModal}
        onClose={props.handleModalToggle}
        actions={[
          <Button
            key="confirm"
            variant="primary"
            onClick={props.handleModalToggle}
          >
            OK
          </Button>
        ]}
      >
        {props.modalText}
      </Modal>
    </React.Fragment>
  );
};

export default ConfirmationModal;
