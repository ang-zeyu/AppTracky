import { getJwt } from "./user";

export async function fetchCustom(apiUrl: string, options: RequestInit): Promise<Response> {
  options.headers = options.headers || {};

  const jwt = getJwt();
  if (jwt) {
    (options.headers as any)['Authorization'] = 'Bearer ' + jwt;
  }

  return await fetch(import.meta.env.VITE_API_BASE_URL + apiUrl, options);
}

export async function getCustom(apiUrl: string, options: RequestInit = {}): Promise<Response> {
  const resp = await fetchCustom(apiUrl, { method: 'POST', ...options });
  if (resp.status !== 200) {
    throw new Error(resp.status + '');
  }

  return resp;
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

  const resp = await fetchCustom(apiUrl, { method: 'POST', ...options });
  if (resp.status !== 200) {
    const respJson = await resp.json();
    throw new Error(respJson.message);
  }

  return resp;
}
