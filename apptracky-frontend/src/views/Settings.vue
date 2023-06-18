<template>
  <v-container>
    <v-row>
      <!-- <v-col cols="2">
        <v-sheet rounded="lg">
          <v-list rounded="lg">
            <v-list-item
              v-for="listItem in listItems"
              :key="listItem.name"
              link
            >
              <v-list-item-action>
                <v-icon :icon="listItem.icon" class="mr-2"></v-icon>
                {{ listItem.name }}
              </v-list-item-action>
            </v-list-item>

            <v-divider class="my-2"></v-divider>

            <v-list-item
              link
              color="grey-lighten-4"
            >
              <v-list-item-title prepend-icon="mdi-logout">
                <v-icon icon="mdi-logout"></v-icon>
                Temp
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-sheet>
      </v-col> -->

      <v-col>
        <v-sheet
          min-height="70vh"
          class="pa-10"
          rounded="lg"
        >
          <v-form v-model:model-value="formState.valid" ref="form">
            <v-text-field
              :model-value="formState.username"
              label="Username"
              required
              disabled
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
              :model-value="formState.email"
              type="email"
              label="Email"
              disabled
              variant="solo-filled"
              prepend-inner-icon="mdi-email"
            ></v-text-field>
          </v-form>
          <v-btn
            block
            class="mb-5"
            @click="changePassword"
          >
            Change Password
          </v-btn>
          <v-btn
            v-if="!user?.isGoogle"
            block
            class="mb-5"
            prepend-icon="mdi-google"
            @click="associateWithGoogle"
            color="blue-lighten-1"
          >
            Associate with Google
          </v-btn>
          <v-alert v-if="didChangePasswordSucceed === true" type="success">
            Success!
          </v-alert>
          <v-alert v-else-if="didChangePasswordSucceed === false" type="error">
            Failed to change password.
          </v-alert>
          <v-alert v-else-if="didAssociationFail" type="error">
            Google account association failed! Did you validate your email?
          </v-alert>
        </v-sheet>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
import { getCustom, postCustom } from '@/utils/fetch';
import { storeOAuthRequest } from '@/utils/oauth';
import { getUser } from '@/utils/user';
import { Ref } from 'vue';
import { reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const didChangePasswordSucceed: Ref<boolean | undefined> = ref();
const didAssociationFail: Ref<boolean> = ref(route.query['success'] === 'false');

const form = ref();
const user = getUser();
const formState = reactive({
  username: user?.username,
  email: user?.email,
  password: '',
  valid: null,
});

const passwordValidation = [
  (v?: string) => (v && v.length >= 8) ? true : 'Password should be at least 8 characters'
];

async function changePassword() {
  form.value.validate();
  if (!formState.valid) {
    return;
  }

  try {
    await postCustom('api/auth/change-password', formState.password);
    didChangePasswordSucceed.value = true;
  } catch (ex) {
    didChangePasswordSucceed.value = false;
    throw ex;
  }
}

async function associateWithGoogle() {
  try {
    const resp = await getCustom('oauth2/authorization/google');
    const redirectUri = resp.headers.get('X-Oauth2-Redirect');
    if (!redirectUri) {
      didAssociationFail.value = true;
      return;
    }

    const oauth2AuthorizationRequest = await resp.arrayBuffer();
    storeOAuthRequest(oauth2AuthorizationRequest);

    window.open(redirectUri, '_self');
  } catch (ex) {
    didAssociationFail.value = true;
    throw ex;
  }
}
</script>
