export interface UserDto {
  jwtToken: string,
  id: number,
  username: string,
  email: string,
  isGoogle: boolean,
  isValidated: boolean,
  roles: string[],
  settings?: UserSettingsDto
}

export interface UserSettingsDto {
  password?: string,
  isReportingEnabled: boolean
}

const USER_KEY = 'user';

export function getUser(): UserDto | null {
  const userRaw = localStorage.getItem(USER_KEY);
  return userRaw ? JSON.parse(userRaw) : null;
}

export function saveUser(user: UserDto) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function removeUser() {
  localStorage.removeItem(USER_KEY);
}

export function getJwt(): string | null {
  const user = getUser();
  if (user) {
    return user.jwtToken;
  }
  return null;
}

export function logout() {
  removeUser();
  location.replace(location.origin);
}
