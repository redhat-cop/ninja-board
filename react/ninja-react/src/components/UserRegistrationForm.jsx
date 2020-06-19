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
import { redHatEmailRegex, usernameRegex } from "../config/Validation";
import API from "../config/ServerUrls";
import ConfirmationModal from "./ConfirmationModal";

/**
 * @author fostimus
 */
const FormSection = props => {
  return (
    <PageSection>
      <UserRegistrationForm />
    </PageSection>
  );
};

export default FormSection;

export class UserRegistrationForm extends React.Component {
  //TODO: use redux or find a different way to manage state better
  state = {
    displayName: "",
    username: {
      value: "",
      invalidText: "",
      helperText: "",
      isValid: true,
      validated: "default"
    },
    email: {
      value: "",
      invalidText: "",
      helperText: "",
      isValid: true,
      validated: "default"
    },
    trello: {
      value: "",
      invalidText: "",
      helperText: "",
      isValid: true,
      validated: "default"
    },
    github: {
      value: "",
      invalidText: "",
      helperText: "",
      isValid: true,
      validated: "default"
    },
    other: "",
    showModal: false,
    modalTitle: "",
    modalText: "",
    clearFormOnSubmit: true
  };

  /**
   * input change handlers
   */
  handleInputChangeDisplayName = displayName => {
    this.setState({ displayName });
  };

  handleInputChangeUsername = username => {
    // ... is the spread operator
    let newState = {
      username: {
        value: username,
        ...this.usernameValidation(username)
      }
    };
    this.setState(newState);
  };

  handleInputChangeEmail = email => {
    const isValid = redHatEmailRegex.test(email);
    if (isValid) {
      this.setState({
        email: {
          value: email,
          isValid: isValid,
          helperText: "Email is valid",
          validated: "success"
        }
      });
    } else {
      this.setState({
        email: {
          value: email,
          isValid: isValid,
          invalidText: "Please follow the format: email@redhat.com",
          helperText: "Validating...",
          validated: "error"
        }
      });
    }
  };

  handleInputChangeTrello = trello => {
    let newState = {
      trello: {
        value: trello,
        ...this.usernameValidation(trello)
      }
    };
    this.setState(newState);
  };

  handleInputChangeGithub = github => {
    let newState = {
      github: {
        value: github,
        ...this.usernameValidation(github)
      }
    };
    this.setState(newState);
  };

  handleInputChangeOther = other => {
    this.setState({ other });
  };

  usernameValidation = value => {
    const isValid = usernameRegex.test(value);
    if (isValid) {
      return {
        isValid: isValid,
        validated: "success"
      };
    } else {
      return {
        isValid: isValid,
        invalidText: "Usernames cannot have spaces",
        helperText: "Validating...",
        validated: "error"
      };
    }
  };

  /**
   * Form Actions (submit, cancel)
   */

  clearForm = () => {
    this.setState({
      displayName: "",
      username: {
        value: "",
        invalidText: "",
        helperText: "",
        isValid: true,
        validated: "default"
      },
      email: {
        value: "",
        invalidText: "",
        helperText: "",
        isValid: true,
        validated: "default"
      },
      trello: {
        value: "",
        invalidText: "",
        helperText: "",
        isValid: true,
        validated: "default"
      },
      github: {
        value: "",
        invalidText: "",
        helperText: "",
        isValid: true,
        validated: "default"
      },
      other: ""
    });
  };

  handleModalToggle = () => {
    this.setState({ showModal: !this.state.showModal });
    if (this.state.clearFormOnSubmit) {
      this.clearForm();
    }
  };

  handleSubmit = event => {
    event.preventDefault();

    const user = {
      displayName: this.state.displayName,
      username: this.state.username.value,
      email: this.state.email.value,
      trelloUsername: this.state.trello.value,
      githubUsername: this.state.github.value
    };

    API.post(`/user`, user)
      .then(res => {
        if (res.status === 201) {
          this.setState({
            showModal: true,
            modalTitle: "Success!",
            modalText:
              "Thank you for registering for the Giveback Ninja Program",
            clearFormOnSubmit: true
          });
        }
      })
      .catch(error => {
        if (error.response) {
          if (error.response.status === 404) {
            this.setState({
              showModal: true,
              modalTitle: "Registration Failed",
              modalText:
                "Kerberos ID not found; please ensure a valid ID for username.",
              clearFormOnSubmit: false
            });
          } else {
            this.setState({
              showModal: true,
              modalTitle: "Registration Failed",
              modalText:
                "Please see an admin for why your registration failed.",
              clearFormOnSubmit: true
            });
          }
          //TODO: add in here checking for specific errors, e.g. LDAP lookup fad, trello/github username not found
        }
        //undefined error response == network error
        else {
          this.setState({
            showModal: true,
            modalTitle: "Network Error",
            modalText:
              "The backend server is down, or your network connection isn't working.",
            clearFormOnSubmit: true
          });
        }
      });
  };

  render() {
    const { displayName, username, email, trello, github, other } = this.state;

    const submitEnabled =
      // not empty
      this.state.displayName !== "" &&
      this.state.username.value !== "" &&
      this.state.email.value !== "" &&
      this.state.trello.value !== "" &&
      this.state.github.value !== "" &&
      // validation passes
      this.state.username.isValid &&
      this.state.email.isValid &&
      this.state.trello.isValid &&
      this.state.github.isValid;

    return (
      <React.Fragment>
        <ConfirmationModal
          showModal={this.state.showModal}
          handleModalToggle={this.handleModalToggle}
          modalTitle={this.state.modalTitle}
          modalText={this.state.modalText}
        />
        <Form>
          <FormGroup
            label="Display Name"
            isRequired
            fieldId="horizontal-form-display-name"
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
            helperTextInvalid={username.invalidText}
            validated={username.validated}
            helperText="User your Kerberos ID"
          >
            <TextInput
              value={username.value}
              validated={username.validated}
              isRequired
              type="text"
              id="horizontal-form-username"
              aria-describedby="horizontal-form-username-helper"
              name="horizontal-form-username"
              onChange={this.handleInputChangeUsername}
            />
          </FormGroup>
          <FormGroup
            label="Email"
            isRequired
            helperText={email.helperText}
            helperTextInvalid={email.invalidText}
            fieldId="horizontal-form-email"
            validated={email.validated}
          >
            <TextInput
              validated={email.validated}
              value={email.value}
              onChange={this.handleInputChangeEmail}
              isRequired
              type="email"
              id="horizontal-form-email"
              name="horizontal-form-email"
            />
          </FormGroup>
          <FormGroup
            label="Trello"
            isRequired
            helperText={trello.helperText}
            helperTextInvalid={trello.invalidText}
            validated={trello.validated}
            fieldId="horizontal-form-trello"
          >
            <TextInput
              value={trello.value}
              validated={trello.validated}
              onChange={this.handleInputChangeTrello}
              isRequired
              type="text"
              id="horizontal-form-trello"
              name="horizontal-form-trello"
            />
          </FormGroup>
          <FormGroup
            label="GitHub"
            isRequired
            helperText={github.helperText}
            helperTextInvalid={github.invalidText}
            validated={github.validated}
            fieldId="horizontal-form-github"
          >
            <TextInput
              value={github.value}
              validated={github.validated}
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
            <Button
              onClick={this.handleSubmit}
              isDisabled={!submitEnabled}
              type="submit"
              variant="primary"
            >
              Submit Form
            </Button>
            <Button onClick={this.clearForm} type="reset" variant="secondary">
              Cancel
            </Button>
          </ActionGroup>
        </Form>
      </React.Fragment>
    );
  }
}
