import axios from "axios";

const local = axios.create({
  baseURL: window.USER_MANAGEMENT_API_URL
});

const dev = {};

const prod = {};

const API = local;

export default API;
