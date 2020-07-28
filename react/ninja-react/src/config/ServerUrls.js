import axios from "axios";

const local = axios.create({
  baseURL: window.USER_MANAGEMENT_API_URL
});

export const userFromToken = jwt => {
  return axios.get(
    'https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=' + jwt
  );
};

const API = local;

export default API;
