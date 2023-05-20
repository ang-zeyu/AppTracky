<template>
  <v-app-bar flat>
    <v-app-bar-title>
      <!-- <v-icon icon="mdi-circle-slice-6" /> -->

      <v-container class="fill-height d-flex align-center">
        <v-avatar
          class="me-10 ms-4"
          color="grey-darken-1"
          size="32"
        ></v-avatar>

        <v-btn
          v-for="link in links"
          :key="link"
          :to="{ name: link }"
          exact
          variant="text"
        >
          {{ link }}
        </v-btn>

        <v-spacer></v-spacer>

        <v-responsive max-width="260">
          <v-text-field
            density="compact"
            hide-details
            variant="solo"
          ></v-text-field>
        </v-responsive>
      </v-container>
    </v-app-bar-title>
    <template #append>
      <v-btn prepend-icon="mdi-logout" @click="logout" v-if="user">
        Logout
      </v-btn>
    </template>
  </v-app-bar>
</template>

<script lang="ts" setup>
import { logout } from '@/utils/user';
import { getUser } from '@/utils/user';

const links: string[] = [];
const user = getUser();
if (user) {
  links.push('Home', 'Settings');
} else {
  links.push('Login');
}
</script>
