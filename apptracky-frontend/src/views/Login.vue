<template>
  <v-container class="fill-height d-flex align-center justify-center">
    <v-card
      title="AppTracky"
      subtitle="Login or Register to continue"
      min-width="500px"
    >
      <v-card-item>
        <v-form v-model:model-value="formState.valid" ref="form">
          <v-text-field
            v-model="formState.username"
            :rules="usernameValidation"
            label="Username"
            required
            variant="solo-filled"
            prepend-inner-icon="mdi-account"
          ></v-text-field>
          <v-text-field
            v-model="formState.password"
            :rules="passwordValidation"
            label="Password"
            type="password"
            required
            variant="solo-filled"
            prepend-inner-icon="mdi-key"
          ></v-text-field>
          <v-text-field
            v-if="formState.isRegister"
            v-model="formState.email"
            :rules="emailValidation"
            type="email"
            label="Email"
            variant="solo-filled"
            prepend-inner-icon="mdi-email"
          ></v-text-field>
        </v-form>
      </v-card-item>
      <v-card-text v-if="!formState.isRegister && formState.isUnauthorized" class="text-red">
        Invalid username or password
      </v-card-text>
      <v-card-text v-if="formState.isRegister && formState.registrationError" class="text-red">
        {{ formState.registrationError }}
      </v-card-text>
      <v-card-item class="flex-wrap">
        <v-btn
          v-if="!formState.isRegister"
          prepend-icon="mdi-google"
          variant="elevated"
          ripple
          block
          color="blue-lighten-1"
          @click="oauthLogin"
        >
          Google Login
        </v-btn>
        <v-btn
          prepend-icon="mdi-login"
          variant="elevated"
          block
          ripple
          color="indigo-lighten-1"
          @click="formState.isRegister ? (formState.isRegister = false) : basicLogin()"
        >
          {{ formState.isRegister ? 'Back to Login' : 'Login' }}
        </v-btn>
        <v-btn
          prepend-icon="mdi-account-plus"
          variant="elevated"
          block
          ripple
          color="light-green-lighten-2"
          @click="formState.isRegister ? basicRegister() : switchToRegister()"
        >
          Register
        </v-btn>
        <v-btn
          v-if="!formState.isRegister"
          variant="flat"
          ripple
          color="white"
          size="small"
          class="text-blue"
          :to="{ name: 'ForgotPw' }"
        >
          Forgot password?
        </v-btn>
      </v-card-item>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { getCustom, postCustom } from '@/utils/fetch';
import { UserDto, saveUser } from '@/utils/user';
import { storeOAuthRequest } from '@/utils/oauth';
import { reactive, ref } from 'vue';

const form = ref();

const formState = reactive({
  username: '',
  password: '',
  email: '',
  isRegister: false,
  isUnauthorized: false,
  registrationError: '',
  valid: null,
});

const usernameValidation = [
  (v?: string) => (v && v.length >= 3) ? true : 'Username should be at least 3 characters'
];

const passwordValidation = [
  (v?: string) => (v && v.length >= 8) ? true : 'Password should be at least 8 characters'
];

const emailValidation = [
  (v?: string) => formState.isRegister
    ? ((v && /^[a-z.-][a-z0-9.-]*@[a-z.-]+\.[a-z]+$/i.test(v)) || 'Invalid email')
    : true
];

function switchToRegister() {
  formState.isRegister = true;
}

async function oauthLogin() {
  const resp = await getCustom('oauth2/authorization/google');
  const redirectUri = resp.headers.get('X-Oauth2-Redirect');
  if (!redirectUri) {
    return; // TODO error
  }

  const oauth2AuthorizationRequest = await resp.arrayBuffer();
  storeOAuthRequest(oauth2AuthorizationRequest);

  window.open(redirectUri, '_self');
}

async function basicLogin() {
  form.value.validate();
  if (!formState.valid) {
    return;
  }

  try {
    const resp = await postCustom('api/auth/login', {}, {
      headers: { 'Authorization': 'Basic ' + btoa(`${formState.username}:${formState.password}`) }
    });

    const user = await resp.json() as UserDto;
    saveUser(user);
    location.href = location.origin;
  } catch (ex) {
    formState.isUnauthorized = true;
    throw ex;
  }
}

async function basicRegister() {
  form.value.validate();
  if (!formState.valid) {
    return;
  }

  try {
    const resp = await postCustom('api/auth/register', { ...formState });
    const user = await resp.json() as UserDto;
    saveUser(user);
    location.href = location.origin;
  } catch (ex: any) {
    formState.registrationError = ex.message;
    throw ex;
  }
}
</script>

<style scoped>
.v-card-item__content > .v-btn {
  margin-top: 10px;
}
</style>
