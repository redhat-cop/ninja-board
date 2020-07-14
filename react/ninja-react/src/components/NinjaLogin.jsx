import React, { useState } from "react";
import GoogleLogin from "react-google-login";
import API from "../config/ServerUrls";
import { Redirect } from "react-router-dom";
import { PageSection } from "@patternfly/react-core";

import axios from "axios";

const LoginSection = params => {
  return (
    <PageSection>
      <NinjaLogin />
    </PageSection>
  )
}

export default LoginSection;

export const NinjaLogin = params => {
  const register = googleResponse => {
    // match this object to ninja user registration API
    const userRegistrationRequest = {
      name: googleResponse.profileObj.name,
      email: googleResponse.profileObj.email,
      token: googleResponse.googleId,
      image: googleResponse.profileObj.imageUrl,
      providerId: "Google"
    };

    // /user API needs to be modified
    API.post(`/user`, userRegistrationRequest).then(res => {
      if (res.status === 201) {
        this.setState({
          showModal: true,
          modalTitle: "Success!",
          modalText: "Thank you for registering for the Giveback Ninja Program",
          clearFormOnSubmit: true
        });
      }
    });
  };

  const responseGoogle = response => {
    console.log(response);
  };

  return (
    <GoogleLogin
      clientId="1029231296777-6hg51pd9kiesovjs89shafotuc9s82go.apps.googleusercontent.com"
      buttonText="Login"
      onSuccess={responseGoogle}
      onFailure={responseGoogle}
      cookiePolicy={"single_host_origin"}
    />
  );
};
