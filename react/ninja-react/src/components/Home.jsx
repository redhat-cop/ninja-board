import React, { Component } from "react";
import { PageSection } from "@patternfly/react-core";

/**
 * @author fostimus
 */ 
export default class HomeSection extends Component {
  render() {
    return (
      <PageSection>
        <Home />
      </PageSection>
    )
  }
}

export class Home extends Component {
  render() {
    return <div>I am home!</div>;
  }
}
