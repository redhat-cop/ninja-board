import React from 'react';
import {
  Form,
  FormGroup,
  TextInput,
  TextArea,
  FormSelect,
  FormSelectOption,
  Checkbox,
  ActionGroup,
  Button,
  Radio
} from '@patternfly/react-core';

export default class UserRegistrationForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      value: 'please choose',
      value1: '',
      value2: '',
      value3: ''
    };
    this.onChange = (value, event) => {
      this.setState({ value });
    };
    this.handleTextInputChange1 = value1 => {
      this.setState({ value1 });
    };
    this.handleTextInputChange2 = value2 => {
      this.setState({ value2 });
    };
    this.handleTextInputChange3 = value3 => {
      this.setState({ value3 });
    };
    this.options = [
      { value: 'please choose', label: 'Please Choose', disabled: false },
      { value: 'mr', label: 'Mr', disabled: false },
      { value: 'miss', label: 'Miss', disabled: false },
      { value: 'mrs', label: 'Mrs', disabled: false },
      { value: 'ms', label: 'Ms', disabled: false },
      { value: 'dr', label: 'Dr', disabled: false },
      { value: 'other', label: 'Other', disabled: false }
    ];
  }

  render() {
    const { value1, value2, value3 } = this.state;

    return (
      <Form isHorizontal>
        <FormGroup
          label="Name"
          isRequired
          fieldId="horizontal-form-name"
          helperText="Please provide your full name"
        >
          <TextInput
            value={value1}
            isRequired
            type="text"
            id="horizontal-form-name"
            aria-describedby="horizontal-form-name-helper"
            name="horizontal-form-name"
            onChange={this.handleTextInputChange1}
          />
        </FormGroup>
        <FormGroup label="Email" isRequired fieldId="horizontal-form-email">
          <TextInput
            value={value2}
            onChange={this.handleTextInputChange2}
            isRequired
            type="email"
            id="horizontal-form-email"
            name="horizontal-form-email"
          />
        </FormGroup>
        <FormGroup label="Your title" fieldId="horizontal-form-title">
          <FormSelect
            value={this.state.value}
            onChange={this.onChange}
            id="horzontal-form-title"
            name="horizontal-form-title"
            aria-label="Your title"
          >
            {this.options.map((option, index) => (
              <FormSelectOption isDisabled={option.disabled} key={index} value={option.value} label={option.label} />
            ))}
          </FormSelect>
        </FormGroup>
        <FormGroup label="Your experience" fieldId="horizontal-form-exp">
          <TextArea
            value={value3}
            onChange={this.handleTextInputChange3}
            name="horizontal-form-exp"
            id="horizontal-form-exp"
          />
        </FormGroup>
        <FormGroup>
          <Checkbox label="Follow up via email" id="alt-form-checkbox-1" name="alt-form-checkbox-1" />
        </FormGroup>
        <FormGroup>
          <Checkbox label="Remember my password for 30 days" id="alt-form-checkbox-2" name="alt-form-checkbox-2" />
        </FormGroup>
        <ActionGroup>
          <Button variant="primary">Submit form</Button>
          <Button variant="secondary">Cancel</Button>
        </ActionGroup>
      </Form>
    );
  }
}
