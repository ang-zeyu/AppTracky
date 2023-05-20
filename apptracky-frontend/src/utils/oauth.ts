import { postCustom } from "./fetch";
import { getUser, saveUser } from "./user";

function arrayBufferToBase64(buffer: ArrayBuffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa( binary );
}

function base64ToArrayBuffer(base64: string) {
  const binaryString = atob(base64);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes.buffer;
}

const OAUTH_REQUEST_KEY = 'OAUTH_REQ';

export function storeOAuthRequest(buffer: ArrayBuffer) {
  const oauth2AuthorizationRequestB64 = arrayBufferToBase64(buffer);
  localStorage.setItem(OAUTH_REQUEST_KEY, oauth2AuthorizationRequestB64);
}

function getOAuthRequest() {
  const oauth2AuthorizationRequestB64 = localStorage.getItem(OAUTH_REQUEST_KEY);
  if (!oauth2AuthorizationRequestB64) {
    return null;
  }

  localStorage.removeItem(OAUTH_REQUEST_KEY);
  return base64ToArrayBuffer(oauth2AuthorizationRequestB64);
}

/*
 * To be called post redirect from the auth server.
 *
 * Extracts the authorization code grant from location.search then
 * manually calls the auth client authorization response endpoint.
 * 
 * This is used for:
 * 1. Logging in / signing up using Google via Login.vue
 * 2. Associating Google account to an existing basic auth account via Settings.vue
 *
 * These 2 cases can be detected by presence of localStorage's user key,
 * allowing client side routing to the appropriate endpoint.
 *
 * Returns true if there was a valid oauth request stored to process, false otherwise.
 */
export async function handleOAuthAuthorizationResponse() {
  const urlParams = new URLSearchParams(location.search);
  const stateParam = urlParams.get('state');
  if (stateParam) {
    const oauthReq = getOAuthRequest();
    if (oauthReq) {
      const user = getUser();
      const wasLoggedIn = !!user;
      const resp = await postCustom(
        'login/oauth2/code/google'
          + location.search
          + (wasLoggedIn ? `&associate=${encodeURIComponent(user.email)}` : ''),
        oauthReq,
      );
      const respJson = await resp.json();
      saveUser(respJson);
      return true;
    }
  }

  return false;
}
