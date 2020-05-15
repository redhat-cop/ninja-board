import React from "react";
import { PageSection } from "@patternfly/react-core";
import {
  Form,
  FormGroup,
  TextInput,
  TextArea,
  ActionGroup,
  Button
} from "@patternfly/react-core";
import axios from "axios";

/**
 * @author fostimus
 */

export default class FormSection extends React.Component {
  render() {
    return (
      <PageSection>
        <UserRegistrationForm />
      </PageSection>
    );
  }
}

export class UserRegistrationForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      displyName: "",
      username: "",
      email: "",
      trello: "",
      github: "",
      other: ""
    };

    this.handleInputChangeDisplayName = displayName => {
      this.setState({ displayName });
    };
    this.handleInputChangeUsername = username => {
      this.setState({ username });
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

  handleSubmit = event => {
    event.preventDefault();

    const user = {
      displyName: this.state.displayName,
      username: this.state.username,
      email: this.state.email,
      trelloUsername: this.state.trello,
      githubUsername: this.state.github
    };

    // update this with Quarkus app url
    // TODO: config this.
    axios
      .post(`https://jsonplaceholder.typicode.com/users`, { user })
      .then(res => {
        console.log(res);
        console.log(res.data);
      });
  };

  render() {
    const { displayName, username, email, trello, github, other } = this.state;

    return (
      <Form>
        <FormGroup
          label="Display Name"
          isRequired
          fieldId="horizontal-form-name"
          helperText="Please provide your full name"
        >
          <TextInput
            value={displayName}
            isRequired
            type="text"
            id="horizontal-form-display-name"
            aria-describedby="horizontal-form-display-name-helper"
            name="horizontal-form-display-name"
            onChange={this.handleInputChangeDisplayName}
          />
        </FormGroup>
        <FormGroup
          label="Username"
          isRequired
          fieldId="horizontal-form-username"
          helperText="User your Kerberos ID"
        >
          <TextInput
            value={username}
            isRequired
            type="text"
            id="horizontal-form-username"
            aria-describedby="horizontal-form-username-helper"
            name="horizontal-form-username"
            onChange={this.handleInputChangeUsername}
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
