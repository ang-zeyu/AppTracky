import { getJwt } from "./user";

export async function fetchCustom(apiUrl: string, options: RequestInit): Promise<Response> {
  options.headers = options.headers || {};

  const jwt = getJwt();
  if (jwt) {
    (options.headers as any)['Authorization'] = 'Bearer ' + jwt;
  }

  const resp = await fetch(import.meta.env.VITE_API_BASE_URL + apiUrl, options);
  if (resp.status !== 200) {
    let respJson;
    try {
      respJson = await resp.json();
    } catch (ex) {
      throw new Error(resp.status + '');
    }

    throw new Error(respJson.message);
  }

  return resp;
}

export async function getCustom(apiUrl: string, options: RequestInit = {}): Promise<Response> {
  return await fetchCustom(apiUrl, { method: 'GET', ...options });
}

export async function postCustom(
  apiUrl: string,
  body: string | ArrayBuffer | any,
  options: RequestInit = {},
): Promise<Response> {
  options.headers = options.headers || {};

  const isJson = typeof body !== 'string' && !('byteLength' in body);
  (options.headers as any)['Content-Type'] = typeof body === 'string'
    ? 'text/plain'
    : (isJson ? 'application/json' : 'application/octet-stream');
  options.body = isJson ? JSON.stringify(body) : body;

  return await fetchCustom(apiUrl, { method: 'POST', ...options });
}

export async function putCustom(
  apiUrl: string,
  body: any,
  options: RequestInit = {},
): Promise<Response> {
  options.headers = options.headers || {};

  (options.headers as any)['Content-Type'] = 'application/json';
  options.body = JSON.stringify(body);

  return await fetchCustom(apiUrl, { method: 'PUT', ...options });
}

export async function delCustom(
  apiUrl: string,
  body: any,
  options: RequestInit = {},
): Promise<Response> {
  options.headers = options.headers || {};

  (options.headers as any)['Content-Type'] = 'application/json';
  options.body = JSON.stringify(body);

  return await fetchCustom(apiUrl, { method: 'DELETE', ...options });
}
