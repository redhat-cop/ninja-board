import axios from "axios";

/* eslint no-control-regex: "off" */
export const emailRegex = /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/;
export const redHatEmailRegex = /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@redhat.com/;
export const usernameRegex = /^\S*$/;

export const validateGithubUsername = username => {
  return axios
    .get("https://api.github.com/search/users?q=" + username)
    .then(response => {
      let validated = false;

      for (let index = 0; index < response.data.items.length; index++) {
        let item = response.data.items[index];
        if (item.login === username) {
          validated = true;
          break;
        }
      }

      return validated;
    });
};

export const validateTrelloUsername = username => {
  return axios
    .get("https://api.trello.com/1/search/members/?query=" + username)
    .then(response => {
      let validated = false;

      for (let index = 0; index < response.data.length; index++) {
        let item = response.data[index];
        if (item.username === username) {
          validated = true;
          break;
        }
      }
      return validated;
    });
};

export const validateJiraUsername = username => {
  return axios
    .get("https://jira.atlassian.com/rest/api/latest/myself")
    .then(response => {
      let validated = false;

      //TODO: no Jira API to validate the username

      return validated;
    });
};
