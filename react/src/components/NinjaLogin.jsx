import React from "react";
import GoogleLogin from "react-google-login";
import API from "../config/ServerUrls";
import { PageSection } from "@patternfly/react-core";

const LoginSection = props => {
  return (
    <PageSection>
      <NinjaLogin {...props} />
    </PageSection>
  );
};

export default LoginSection;

export const NinjaLogin = props => {
  const register = googleResponse => {
    const headers = {
      headers: { Authorization: `Bearer ${googleResponse.tokenObj}` }
    };

    // NOTE: username is not sent in google response
    const userRegistrationRequest = {
      firstName: googleResponse.profileObj.givenName,
      lastName: googleResponse.profileObj.familyName,
      email: googleResponse.profileObj.email,
      imageUrl: googleResponse.profileObj.imageUrl
    };

    console.log(props);

    API.post(`/user`, userRegistrationRequest, headers)
      .then(res => {
        //intended state: by logging in some information (name, email, profile picture) will already be set on the profile. should be taken to "edit" page, instead of "registration" page
        props.setLoggedIn(true);
        props.history.push("/registration-form");
      })
      //TODO: fill in error handling
      .catch(error => {
        if (error.response) {
        }
        //undefined error response == network error
        else {

        }
        //temporary way to test log in works, but this is when the network or server is down
        props.setLoggedIn(true);
        props.history.push("/registration-form");
      });
  };

  const responseGoogle = response => {
    console.log(response);
    //store jwt in local storage, to enable user sessions
    localStorage.setItem("jwt-token", response.tokenId);
    localStorage.setItem("display-name", response.profileObj.name);
    register(response);
  };

  //TODO: client id should be stored in OCP configmap
  return (
    <GoogleLogin
      clientId="1029231296777-6hg51pd9kiesovjs89shafotuc9s82go.apps.googleusercontent.com"
      buttonText="Login"
      onSuccess={responseGoogle}
      onFailure={responseGoogle}
      cookiePolicy={"none"}
    />
  );
};
