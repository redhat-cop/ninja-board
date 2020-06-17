import React from "react";
import { Modal, Button } from "@patternfly/react-core";

export default class ConfirmationModal extends React.Component {


  render() {
    return (
      <React.Fragment>
        <Modal
          variant="small"
          title={this.props.modalTitle}
          isOpen={this.props.showModal}
          onClose={this.props.handleModalToggle}
          actions={[
            <Button
              key="confirm"
              variant="primary"
              onClick={this.props.handleModalToggle}
            >
              OK
            </Button>,
          ]}
        >
          {this.props.modalText}
        </Modal>
      </React.Fragment>
    );
  }
}
