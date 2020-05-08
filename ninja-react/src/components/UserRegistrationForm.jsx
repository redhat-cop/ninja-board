import React from "react";
import {
  Form,
  FormGroup,
  TextInput,
  TextArea,
  ActionGroup,
  Button
} from "@patternfly/react-core";

export default class UserRegistrationForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      name: "",
      email: "",
      trello: "",
      github: "",
      other: ""
    };

    this.handleInputChangeName = name => {
      this.setState({ name });
    };
    this.handleInputChangeEmail = email => {
      this.setState({ email });
    };
    this.handleInputChangeTrello = trello => {
      this.setState({ trello });
    };
    this.handleInputChangeGithub = github => {
      this.setState({ github });
    };
    this.handleInputChangeOther = other => {
      this.setState({ other });
    };
  }

  render() {
    const { name, email, trello, github, other } = this.state;

    return (
      <Form>
        <FormGroup
          label="Name"
          isRequired
          fieldId="horizontal-form-name"
          helperText="Please provide your full name"
        >
          <TextInput
            value={name}
            isRequired
            type="text"
            id="horizontal-form-name"
            aria-describedby="horizontal-form-name-helper"
            name="horizontal-form-name"
            onChange={this.handleInputChangeName}
          />
        </FormGroup>
        <FormGroup label="Email" isRequired fieldId="horizontal-form-email">
          <TextInput
            value={email}
            onChange={this.handleInputChangeEmail}
            isRequired
            type="email"
            id="horizontal-form-email"
            name="horizontal-form-email"
          />
        </FormGroup>
        <FormGroup label="Trello" isRequired fieldId="horizontal-form-trello">
          <TextInput
            value={trello}
            onChange={this.handleInputChangeTrello}
            isRequired
            type="text"
            id="horizontal-form-trello"
            name="horizontal-form-trello"
          />
        </FormGroup>
        <FormGroup label="GitHub" isRequired fieldId="horizontal-form-github">
          <TextInput
            value={github}
            onChange={this.handleInputChangeGithub}
            isRequired
            type="text"
            id="horizontal-form-github"
            name="horizontal-form-github"
          />
        </FormGroup>
        <FormGroup
          label="Please provide links to any other qualifying CoP Contributions that do not show up in GitHub or Trello"
          fieldId="horizontal-form-exp"
        >
          <TextArea
            value={other}
            onChange={this.handleInputChangeOther}
            name="horizontal-form-exp"
            id="horizontal-form-exp"
          />
        </FormGroup>
        <ActionGroup>
          <Button variant="primary">Submit form</Button>
          <Button variant="secondary">Cancel</Button>
        </ActionGroup>
      </Form>
    );
  }
}
