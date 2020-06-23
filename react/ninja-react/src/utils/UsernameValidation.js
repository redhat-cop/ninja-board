import axios from "axios";

export const validateGithubUsername = async username => {
  await axios
    .get("https://api.github.com/search/users?q=" + username)
    .then(response => {
      let validated = false;

      for (let index = 0; index < response.data.items.length; index++) {
        let item = response.data.items[index];
        if (item.login === username) {
          validated = true;
          break;
        }
        index++;
      }

      return validated;
      console.log(response.data);
      console.log(response.data.items);
    });
};
