<template>
  <v-container class="fill-height d-flex align-center justify-center">
    <v-card
      title="AppTracky"
      min-width="500px"
    >
      <v-card-item v-if="!sentPasswordReset">
        <v-text-field
          v-model="password"
          :rules="passwordValidation"
          label="Password"
          type="password"
          required
          variant="solo-filled"
          prepend-inner-icon="mdi-key"
          ref="passwordField"
        ></v-text-field>
      </v-card-item>
      <v-card-item class="flex-wrap">
        <v-btn
          v-if="!sentPasswordReset"
          prepend-icon="mdi-key"
          variant="elevated"
          block
          ripple
          class="mb-3"
          @click="sendPasswordReset"
        >
          Reset
        </v-btn>
        <v-btn
          prepend-icon="mdi-login"
          variant="elevated"
          block
          ripple
          color="indigo-lighten-1"
          :to="{ name: 'Login' }"
        >
          Back to Login
        </v-btn>
      </v-card-item>
      <v-card-item>
        <v-alert type="success" v-if="sentPasswordReset">
          Success! Try logging in with your new password.
        </v-alert>
        <v-alert type="error" v-if="error">
          {{ error }}
        </v-alert>
      </v-card-item>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { postCustom } from '@/utils/fetch';
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const password = ref('');
const sentPasswordReset = ref(false);
const passwordField = ref();
const error = ref('');

const route = useRoute();
const uuid = route.query.uuid as string;

const passwordValidation = [
  (v?: string) => (v && v.length >= 8) ? true : 'Password should be at least 8 characters'
];

async function sendPasswordReset() {
  if (!passwordField.value.checkValidity()) {
    return;
  }

  try {
    await postCustom('api/auth/reset-password', { password: password.value, uuid });

    error.value = '';
    sentPasswordReset.value = true;
  } catch (ex: any) {
    error.value = ex.message;
    throw ex;
  }
}
</script>
