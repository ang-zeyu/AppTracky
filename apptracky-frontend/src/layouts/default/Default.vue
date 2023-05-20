<template>
  <v-app>
    <default-bar />
    <default-view />
  </v-app>
</template>

<script lang="ts" setup>
import { handleOAuthAuthorizationResponse } from '@/utils/oauth';
import DefaultBar from './AppBar.vue'
import DefaultView from './View.vue'
import { useRouter } from 'vue-router';
import { getUser } from '@/utils/user';
  
// -------------------------------------------
// This handles giving the authorization response to the oauth client
// after being redirected back from the auth server.
// If there is one.

const router = useRouter();
const wasLoggedIn = !!getUser();
function redirectOAuth(successful: boolean) {
  // Remove all OAuth query parameters and redirect
  if (wasLoggedIn) {
    // Associate google account request
    // TODO put user into global store (Pinia) to refresh appbar, instead of using location.href refresh
    //history.replaceState({}, '', location.origin + '/#/settings'); // remove query parameters
    //router.push({ name: 'Settings', query: { success: successful + '' } });
    location.href = location.origin + '/#/settings?success=' + successful;
  } else {
    // Signup/login request
    //history.replaceState({}, '', location.origin + '/#/');         // remove query parameters
    //router.push({ name: 'Home', query: { success: successful + '' } });
    location.href = location.origin + '/#/';
  }
}

handleOAuthAuthorizationResponse()
  .then((didProcess) => {
    if (!didProcess) {
      // Nothing to process
      return;
    }

    redirectOAuth(true);
  })
  .catch((err) => {
    redirectOAuth(false);
    throw err;
  });

// -------------------------------------------
</script>
