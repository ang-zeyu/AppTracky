<template>
  <v-container class="fill-height d-flex align-center justify-center">
    <v-card
      title="AppTracky"
      min-width="500px"
    >
      <v-card-item v-if="!sentEmail">
        <v-text-field
          v-model="email"
          label="Email"
          required
          variant="solo-filled"
          prepend-inner-icon="mdi-email"
        ></v-text-field>
      </v-card-item>
      <v-card-item class="flex-wrap">
        <v-btn
          v-if="!sentEmail"
          prepend-icon="mdi-email"
          variant="elevated"
          block
          ripple
          class="mb-3"
          @click="sendEmail"
        >
          Send email
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
        <v-alert type="success" v-if="sentEmail">
          Success! An email will be sent if a user matching the specified email exists.
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

const email = ref('');
const sentEmail = ref(false);
const error = ref('');

async function sendEmail() {
  try {
    await postCustom('api/auth/forgot-password', email.value);

    error.value = '';
    sentEmail.value = true;
  } catch (ex: any) {
    error.value = ex.message;
    throw ex;
  }
}
</script>
